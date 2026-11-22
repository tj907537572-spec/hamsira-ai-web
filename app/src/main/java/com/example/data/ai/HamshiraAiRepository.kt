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
import org.json.JSONObject
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
        val payload = buildRequestJson(
            systemInstruction = buildSystemInstruction(language, responseStyle),
            history = history,
            userPrompt = userPrompt,
            attachment = attachment
        )

        val request = Request.Builder()
            .url(endpointUrl)
            .addHeader("Accept", "application/json, text/event-stream")
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .post(payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()

        val call = httpClient.newCall(request)
        val accumulated = StringBuilder()

        try {
            withTimeout(GENERATION_TIMEOUT_MS) {
                call.execute().use { response ->
                    if (!response.isSuccessful) {
                        val errorBody = response.body?.string().orEmpty()
                        val detail = extractApiError(errorBody)
                        val msg = if (detail.isNotBlank()) {
                            "Server (${response.code}): $detail"
                        } else {
                            "Server HTTP ${response.code}"
                        }
                        throw IllegalStateException(msg)
                    }

                    val body = response.body
                        ?: throw IllegalStateException(emptyResponseMessage(language))
                    val contentType = response.header("Content-Type").orEmpty()

                    if (contentType.contains("text/event-stream", ignoreCase = true)) {
                        val source = body.source()
                        while (!source.exhausted()) {
                            val line = source.readUtf8Line() ?: break
                            if (!line.startsWith("data:")) continue
                            val data = line.removePrefix("data:").trim()
                            if (data.isEmpty() || data == "[DONE]") continue

                            val chunkText = extractTextFromGenerateContentJson(data)
                            if (chunkText.isNotEmpty()) {
                                accumulated.append(chunkText)
                                emit(accumulated.toString())
                            }
                        }
                    } else {
                        val rawBody = body.string()
                        if (rawBody.trimStart().startsWith("data:")) {
                            rawBody.lineSequence()
                                .filter { it.startsWith("data:") }
                                .map { it.removePrefix("data:").trim() }
                                .filter { it.isNotEmpty() && it != "[DONE]" }
                                .forEach { data ->
                                    val chunkText = extractTextFromGenerateContentJson(data)
                                    if (chunkText.isNotEmpty()) {
                                        accumulated.append(chunkText)
                                    }
                                }
                            if (accumulated.isNotBlank()) {
                                emit(accumulated.toString())
                            }
                        } else {
                            val apiError = extractApiErrorFromJsonOnly(rawBody)
                            val parsedText = extractTextFromGenerateContentJson(rawBody)
                            if (parsedText.isNotBlank()) {
                                accumulated.append(parsedText)
                                emit(parsedText)
                            } else if (apiError.isNotBlank()) {
                                throw IllegalStateException(apiError)
                            }
                        }
                    }
                }
            }

            if (accumulated.isBlank()) {
                throw IllegalStateException(emptyResponseMessage(language))
            }
        } catch (e: TimeoutCancellationException) {
            call.cancel()
            Log.e(TAG, "Backend AI consultation timed out after ${GENERATION_TIMEOUT_MS}ms", e)
            throw e
        } catch (e: CancellationException) {
            call.cancel()
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Backend AI consultation failed", e)
            throw e
        }
    }.flowOn(Dispatchers.IO)

    internal fun buildRequestJson(
        systemInstruction: String,
        history: List<ChatMessage>,
        userPrompt: String,
        attachment: MediaAttachment?
    ): JSONObject {
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
        if (attachment != null) {
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
        }
        currentUserParts.put(JSONObject().put("text", userPrompt))

        contentsArray.put(
            JSONObject().apply {
                put("role", "user")
                put("parts", currentUserParts)
            }
        )

        return JSONObject().apply {
            put(
                "systemInstruction",
                JSONObject().put(
                    "parts",
                    JSONArray().put(JSONObject().put("text", systemInstruction))
                )
            )
            put("contents", contentsArray)
            put(
                "generationConfig",
                JSONObject().apply {
                    put("temperature", 0.45)
                    put("topP", 0.9)
                }
            )
        }
    }

    internal fun extractTextFromGenerateContentJson(jsonString: String): String {
        if (jsonString.isBlank()) return ""
        return try {
            val root = JSONObject(jsonString)
            val candidates = root.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val content = candidates.optJSONObject(0)?.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null) {
                    val sb = StringBuilder()
                    for (i in 0 until parts.length()) {
                        val partText = parts.optJSONObject(i)?.optString("text", "").orEmpty()
                        sb.append(partText)
                    }
                    if (sb.isNotEmpty()) return sb.toString()
                }
            }
            root.optString("text", "")
                .ifBlank { root.optString("reply", "") }
                .ifBlank { root.optString("answer", "") }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse generateContent JSON response", e)
            ""
        }
    }

    private fun extractApiErrorFromJsonOnly(body: String): String {
        if (body.isBlank()) return ""
        return try {
            val root = JSONObject(body)
            val errObj = root.optJSONObject("error")
            if (errObj != null) {
                errObj.optString("message", "").ifBlank { errObj.toString() }
            } else {
                root.optString("error", "")
            }
        } catch (_: Exception) {
            ""
        }
    }

    private fun extractApiError(errorBody: String): String {
        if (errorBody.isBlank()) return ""
        return extractApiErrorFromJsonOnly(errorBody).ifBlank {
            errorBody.take(160)
        }
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
