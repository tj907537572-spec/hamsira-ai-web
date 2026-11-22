package com.example.data.ai

import android.util.Log
import com.example.core.MediaAttachmentHelper
import com.example.data.model.AppLanguage
import com.example.data.model.ChatMessage
import com.example.data.model.MediaAttachment
import com.example.data.model.ResponseStyle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withTimeout
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

private const val TAG = "HamshiraAiRepository"
const val BACKEND_CHAT_PROXY_URL = "https://hamsira-ai-web.vercel.app/api/chat"
private const val GENERATION_TIMEOUT_MS = 90_000L

interface NurseAiClient {
    fun streamConsultation(
        history: List<ChatMessage>,
        userPrompt: String,
        language: AppLanguage,
        responseStyle: ResponseStyle,
        attachment: MediaAttachment? = null
    ): Flow<String>
}

class BackendConsultationException(message: String, cause: Throwable? = null) :
    IllegalStateException(message, cause)

class HamshiraAiRepository(
    private val httpClient: OkHttpClient = defaultHttpClient(),
    private val endpointUrl: String = BACKEND_CHAT_PROXY_URL
) : NurseAiClient {

    override fun streamConsultation(
        history: List<ChatMessage>,
        userPrompt: String,
        language: AppLanguage,
        responseStyle: ResponseStyle,
        attachment: MediaAttachment?
    ): Flow<String> = flow {
        val trimmedPrompt = userPrompt.trim()
        if (trimmedPrompt.isEmpty() && attachment == null) {
            throw BackendConsultationException(emptyPromptMessage(language))
        }

        val payload = buildRequestJson(
            language = language,
            responseStyle = responseStyle,
            history = history,
            userPrompt = trimmedPrompt,
            attachment = attachment
        )

        val request = Request.Builder()
            .url(endpointUrl)
            .addHeader("Accept", "application/json")
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .post(payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()

        val call = httpClient.newCall(request)

        try {
            val responseText = withTimeout(GENERATION_TIMEOUT_MS) {
                call.execute().use { response ->
                    val rawBody = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        val detail = extractApiError(rawBody)
                        throw BackendConsultationException(
                            httpErrorMessage(language, response.code, detail)
                        )
                    }

                    if (rawBody.isBlank()) {
                        throw BackendConsultationException(emptyResponseMessage(language))
                    }

                    parseGenerateContentResponse(rawBody, language)
                }
            }

            if (responseText.isBlank()) {
                throw BackendConsultationException(emptyResponseMessage(language))
            }
            emit(responseText)
        } catch (e: TimeoutCancellationException) {
            call.cancel()
            Log.e(TAG, "Backend AI consultation timed out after ${GENERATION_TIMEOUT_MS}ms", e)
            throw e
        } catch (e: CancellationException) {
            call.cancel()
            throw e
        } catch (e: BackendConsultationException) {
            Log.e(TAG, "Backend AI consultation error: ${e.message}", e)
            throw e
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "Socket timeout talking to backend proxy", e)
            throw BackendConsultationException(timeoutErrorMessage(language), e)
        } catch (e: UnknownHostException) {
            Log.e(TAG, "No DNS/network resolution for backend proxy", e)
            throw BackendConsultationException(networkUnavailableMessage(language), e)
        } catch (e: ConnectException) {
            Log.e(TAG, "Connection refused or unreachable backend proxy", e)
            throw BackendConsultationException(networkUnavailableMessage(language), e)
        } catch (e: IOException) {
            Log.e(TAG, "Network I/O failure talking to backend proxy", e)
            throw BackendConsultationException(networkIoErrorMessage(language), e)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during backend AI consultation", e)
            throw BackendConsultationException(unexpectedErrorMessage(language), e)
        }
    }.flowOn(Dispatchers.IO)

    internal fun buildRequestJson(
        language: AppLanguage,
        responseStyle: ResponseStyle,
        history: List<ChatMessage>,
        userPrompt: String,
        attachment: MediaAttachment?
    ): JSONObject {
        val systemInstruction = buildSystemInstruction(language, responseStyle)
        val composedPrompt = buildComposedPrompt(
            systemInstruction = systemInstruction,
            history = history,
            userPrompt = userPrompt
        )

        if (attachment == null) {
            return JSONObject().apply {
                put("prompt", composedPrompt)
            }
        }

        val contentsArray = JSONArray()
        history
            .filter { !it.isError && (it.content.isNotBlank() || it.attachmentUri != null) }
            .takeLast(12)
            .forEach { msg ->
                val historyText = when {
                    msg.content.isNotBlank() && msg.attachmentUri != null -> {
                        val marker = if (msg.attachmentIsVideo) "[Замимаи видео]" else "[Замимаи акс]"
                        "$marker ${msg.content}"
                    }
                    msg.content.isNotBlank() -> msg.content
                    msg.attachmentIsVideo -> "[Замимаи видео]"
                    else -> "[Замимаи акс]"
                }
                contentsArray.put(
                    JSONObject().apply {
                        put("role", if (msg.isUser) "user" else "model")
                        put("parts", JSONArray().put(JSONObject().put("text", historyText)))
                    }
                )
            }

        val currentUserParts = JSONArray()
        val base64Data = MediaAttachmentHelper.readBase64Bytes(attachment.uriString)
        if (!base64Data.isNullOrBlank()) {
            currentUserParts.put(
                JSONObject().put(
                    "inline_data",
                    JSONObject().apply {
                        put("mime_type", attachment.mimeType)
                        put("data", base64Data)
                    }
                )
            )
        }
        currentUserParts.put(JSONObject().put("text", userPrompt))

        contentsArray.put(
            JSONObject().apply {
                put("role", "user")
                put("parts", currentUserParts)
            }
        )

        return JSONObject().apply {
            put("prompt", composedPrompt)
            put(
                "systemInstruction",
                JSONObject().put(
                    "parts",
                    JSONArray().put(JSONObject().put("text", systemInstruction))
                )
            )
            put("contents", contentsArray)
        }
    }

    internal fun buildComposedPrompt(
        systemInstruction: String,
        history: List<ChatMessage>,
        userPrompt: String
    ): String {
        val recentHistory = history
            .filter { !it.isError && (it.content.isNotBlank() || it.attachmentUri != null) }
            .takeLast(8)

        val sb = StringBuilder()
        sb.append(systemInstruction)
        if (recentHistory.isNotEmpty()) {
            sb.append("\n\nКонтекст предыдущих сообщений:\n")
            recentHistory.forEach { msg ->
                val speaker = if (msg.isUser) "Пользователь" else "Ҳамшира AI"
                val marker = when {
                    msg.attachmentUri == null -> ""
                    msg.attachmentIsVideo -> "[Видео] "
                    else -> "[Фото] "
                }
                sb.append("- ").append(speaker).append(": ").append(marker).append(msg.content).append('\n')
            }
        }
        sb.append("\nТекущий запрос пользователя:\n").append(userPrompt)
        return sb.toString()
    }

    internal fun parseGenerateContentResponse(jsonString: String, language: AppLanguage): String {
        val root = try {
            JSONObject(jsonString)
        } catch (e: JSONException) {
            Log.w(TAG, "Invalid JSON received from backend proxy", e)
            throw BackendConsultationException(invalidJsonMessage(language), e)
        }

        val apiError = extractApiErrorFromJsonObject(root)
        if (apiError.isNotBlank()) {
            throw BackendConsultationException(serverReportedErrorMessage(language, apiError))
        }

        val candidates = root.optJSONArray("candidates")
        if (candidates != null && candidates.length() > 0) {
            val firstCandidate = candidates.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            if (parts != null && parts.length() > 0) {
                val sb = StringBuilder()
                for (i in 0 until parts.length()) {
                    val partText = parts.optJSONObject(i)?.optString("text", "").orEmpty()
                    sb.append(partText)
                }
                val combined = sb.toString().trim()
                if (combined.isNotEmpty()) {
                    return combined
                }
            }

            val finishReason = firstCandidate?.optString("finishReason", "").orEmpty()
            if (finishReason.equals("SAFETY", ignoreCase = true)) {
                throw BackendConsultationException(safetyBlockedMessage(language))
            }
        }

        val blockReason = root.optJSONObject("promptFeedback")?.optString("blockReason", "").orEmpty()
        if (blockReason.isNotBlank()) {
            throw BackendConsultationException(safetyBlockedMessage(language))
        }

        val fallbackText = root.optString("text", "")
            .ifBlank { root.optString("reply", "") }
            .ifBlank { root.optString("answer", "") }
            .trim()
        if (fallbackText.isNotEmpty()) {
            return fallbackText
        }

        throw BackendConsultationException(emptyResponseMessage(language))
    }

    private fun extractApiErrorFromJsonObject(root: JSONObject): String {
        val errObj = root.optJSONObject("error")
        if (errObj != null) {
            return errObj.optString("message", "").ifBlank { errObj.toString() }
        }
        return root.optString("error", "")
    }

    private fun extractApiError(errorBody: String): String {
        if (errorBody.isBlank()) return ""
        return try {
            extractApiErrorFromJsonObject(JSONObject(errorBody)).ifBlank {
                errorBody.trim().take(160)
            }
        } catch (_: Exception) {
            errorBody.trim().take(160)
        }
    }

    private fun httpErrorMessage(language: AppLanguage, code: Int, detail: String): String {
        val base = when (code) {
            400 -> when (language) {
                AppLanguage.TG -> "Дархости нодуруст ба сервер (400)."
                AppLanguage.RU -> "Некорректный запрос к серверу (400)."
                AppLanguage.UZ_LATN -> "Serverga noto'g'ri so'rov yuborildi (400)."
            }
            404 -> when (language) {
                AppLanguage.TG -> "Хизмати сервер ёфт нашуд (404)."
                AppLanguage.RU -> "Сервис сервера не найден (404)."
                AppLanguage.UZ_LATN -> "Server xizmati topilmadi (404)."
            }
            405 -> when (language) {
                AppLanguage.TG -> "Усули дархост аз ҷониби сервер дастгирӣ намешавад (405)."
                AppLanguage.RU -> "Метод запроса не поддерживается сервером (405)."
                AppLanguage.UZ_LATN -> "So'rov usuli server tomonidan qo'llab-quvvatlanmaydi (405)."
            }
            413 -> when (language) {
                AppLanguage.TG -> "Ҳаҷми акс ё видео барои фиристодан ба сервер хеле калон аст. Файли хурдтарро интихоб кунед."
                AppLanguage.RU -> "Размер фото или видео слишком велик для отправки на сервер. Выберите файл поменьше."
                AppLanguage.UZ_LATN -> "Rasm yoki video hajmi server uchun juda katta. Kichikroq fayl tanlang."
            }
            429 -> when (language) {
                AppLanguage.TG -> "Дархостҳо хеле зиёданд. Лутфан пас аз чанд лаҳза аз нав кӯшиш кунед."
                AppLanguage.RU -> "Слишком много запросов. Пожалуйста, повторите попытку чуть позже."
                AppLanguage.UZ_LATN -> "So'rovlar juda ko'p. Iltimos, birozdan keyin qayta urinib ko'ring."
            }
            in 500..599 -> when (language) {
                AppLanguage.TG -> "Хатои сервери Ҳамшира AI ($code)."
                AppLanguage.RU -> "Ошибка сервера Ҳамшира AI ($code)."
                AppLanguage.UZ_LATN -> "Hamshira AI serverida xatolik ($code)."
            }
            else -> when (language) {
                AppLanguage.TG -> "Хатои пайвастшавӣ ба сервер (HTTP $code)."
                AppLanguage.RU -> "Ошибка связи с сервером (HTTP $code)."
                AppLanguage.UZ_LATN -> "Server bilan bog'lanishda xatolik (HTTP $code)."
            }
        }
        return if (detail.isNotBlank() && code !in setOf(404, 405, 413)) {
            "$base ($detail)"
        } else {
            base
        }
    }

    private fun serverReportedErrorMessage(language: AppLanguage, detail: String): String = when (language) {
        AppLanguage.TG -> "Хатои сервер: $detail"
        AppLanguage.RU -> "Ошибка сервера: $detail"
        AppLanguage.UZ_LATN -> "Server xatosi: $detail"
    }

    private fun invalidJsonMessage(language: AppLanguage): String = when (language) {
        AppLanguage.TG -> "Ҷавоби сервер дар шакли нодуруст омад. Лутфан баъдтар аз нав кӯшиш кунед."
        AppLanguage.RU -> "Сервер вернул ответ в неожиданном формате. Попробуйте позже."
        AppLanguage.UZ_LATN -> "Serverdan kutilmagan formatdagi javob keldi. Keyinroq urinib ko'ring."
    }

    private fun safetyBlockedMessage(language: AppLanguage): String = when (language) {
        AppLanguage.TG -> "Ба ин дархост ҷавоб додан имконнопазир аст. Саволро ором ва муфассалтар нависед ё ба 103 муроҷиат кунед."
        AppLanguage.RU -> "Не удалось сформировать ответ по данному запросу. Переформулируйте вопрос или при срочных симптомах звоните 103."
        AppLanguage.UZ_LATN -> "Ushbu so'rovga javob berib bo'lmadi. Savolni boshqacha yozing yoki shoshilinch holatda 103 ga murojaat qiling."
    }

    private fun emptyPromptMessage(language: AppLanguage): String = when (language) {
        AppLanguage.TG -> "Лутфан саволи худро нависед ё аксу видео илова кунед."
        AppLanguage.RU -> "Пожалуйста, введите вопрос или прикрепите фото/видео."
        AppLanguage.UZ_LATN -> "Iltimos, savol yozing yoki rasm/video biriktiring."
    }

    private fun networkUnavailableMessage(language: AppLanguage): String = when (language) {
        AppLanguage.TG -> "Ба сервер пайваст шудан нашуд. Пайвасти интернетро санҷед."
        AppLanguage.RU -> "Не удалось подключиться к серверу. Проверьте подключение к интернету."
        AppLanguage.UZ_LATN -> "Serverga ulanib bo'lmadi. Internet aloqasini tekshiring."
    }

    private fun networkIoErrorMessage(language: AppLanguage): String = when (language) {
        AppLanguage.TG -> "Мушкилии шабака ҳангоми ирсоли дархост. Аз нав кӯшиш кунед."
        AppLanguage.RU -> "Сетевая ошибка при отправке запроса. Повторите попытку."
        AppLanguage.UZ_LATN -> "So'rov yuborishda tarmoq xatosi yuz berdi. Qayta urinib ko'ring."
    }

    private fun timeoutErrorMessage(language: AppLanguage): String = when (language) {
        AppLanguage.TG -> "Вақти интизории ҷавоб ба охир расид. Пайвастро санҷед ва аз нав кӯшиш кунед."
        AppLanguage.RU -> "Превышено время ожидания ответа от сервера. Повторите попытку."
        AppLanguage.UZ_LATN -> "Serverdan javob kutish vaqti tugadi. Qayta urinib ko'ring."
    }

    private fun unexpectedErrorMessage(language: AppLanguage): String = when (language) {
        AppLanguage.TG -> "Аз сервер ҷавоб гирифта нашуд. Лутфан баъдтар кӯшиш кунед."
        AppLanguage.RU -> "Не удалось получить ответ от сервера. Пожалуйста, попробуйте позже."
        AppLanguage.UZ_LATN -> "Serverdan javob olib bo'lmadi. Keyinroq qayta urinib ko'ring."
    }

    private fun emptyResponseMessage(language: AppLanguage): String = when (language) {
        AppLanguage.TG -> "Ёрдамчӣ ҷавоб сохта натавонист. Саволро равшантар нависед."
        AppLanguage.RU -> "Ассистент не смог сформировать ответ. Попробуйте переформулировать вопрос."
        AppLanguage.UZ_LATN -> "Yordamchi javob tuza olmadi. Savolni boshqacha yozib ko'ring."
    }

    private fun buildSystemInstruction(
        language: AppLanguage,
        responseStyle: ResponseStyle
    ): String = """
        Ты — «Ҳамшира AI» («Медсестра AI»), спокойный, заботливый и надёжный цифровой медицинский ассистент-медсестра.
        
        Язык и тон:
        - ${language.aiLanguageInstruction}
        - ${responseStyle.aiStyleInstruction}
        - Пиши спокойно, структурированно, без сложных непонятных терминов (если термин нужен — сразу коротко поясняй простыми словами).
        
        Работа с фото и видео (если пользователь прикрепил изображение или короткое видео):
        - Опиши наблюдаемые внешние признаки нейтрально и бережно, как информационное наблюдение медсестры (например: покраснение, отёк, локализация), без постановки медицинского диагноза по картинке/видео.
        - Если изображение или видео нечёткое, уточни, какие дополнительные детали важны, и напомни о важности очного осмотра врача.
        - Если видны тревожные признаки (сильное кровотечение, обширный ожог, тяжёлая травма, затруднённое дыхание, нарушение сознания), сразу рекомендуй вызвать скорую помощь (103 / 112).
        
        Безопасность и границы:
        - Ты НЕ ставишь диагнозы и НЕ назначаешь рецептурные препараты или индивидуальные дозировки лекарств.
        - Всегда подчёркивай важность очного осмотра врача при сохранении или усилении симптомов.
        - При опасных симптомах («красных флагах») прямо рекомендуй немедленно вызвать скорую помощь (103 / 112).
        
        Форматирование ответа (используй именно такие теги в начале строки для ключевых блоков, чтобы интерфейс аккуратно их выделил):
        - Для заголовков разделов используй `### Заголовок`
        - Для списков используй `- пункт`
        - Выделяй ключевые слова **жирным**.
        - Используй префикс `[СОВЕТ]` (или `[МАСЛИҲАТ]` на таджикском) для блока полезных безопасных шагов ухода (зелёный акцент).
        - Используй префикс `[ВНИМАНИЕ]` (или `[ДИҚҚАТ]` на таджикском) для важных ограничений/напоминаний (янтарный акцент).
        - Используй префикс `[СРОЧНО К ВРАЧУ]` (или `[ФАВРАН БА ДУХТУР]` на таджикском) для тревожных «красных флагов», при которых нужна срочная медицинская помощь (коралловый акцент).
    """.trimIndent()

    companion object {
        private fun defaultHttpClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }
}
