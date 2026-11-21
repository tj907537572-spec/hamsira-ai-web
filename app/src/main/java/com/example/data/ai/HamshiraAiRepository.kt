package com.example.data.ai

import android.util.Log
import com.example.data.model.AppLanguage
import com.example.data.model.ChatMessage
import com.example.data.model.ResponseStyle
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "HamshiraAiRepository"
private const val MODEL_NAME = "gemini-3.8-flash"
private const val GENERATION_TIMEOUT_MS = 60_000L

interface NurseAiClient {
    fun streamConsultation(
        history: List<ChatMessage>,
        userPrompt: String,
        language: AppLanguage,
        responseStyle: ResponseStyle
    ): Flow<String>
}

class HamshiraAiRepository : NurseAiClient {

    private val defaultModel: GenerativeModel by lazy {
        createModel(AppLanguage.TG, ResponseStyle.BALANCED)
    }

    private val modelCache = ConcurrentHashMap<Pair<AppLanguage, ResponseStyle>, GenerativeModel>()

    private fun getModel(language: AppLanguage, responseStyle: ResponseStyle): GenerativeModel {
        if (language == AppLanguage.TG && responseStyle == ResponseStyle.BALANCED) {
            return defaultModel
        }
        return modelCache.getOrPut(language to responseStyle) {
            createModel(language, responseStyle)
        }
    }

    private fun createModel(language: AppLanguage, responseStyle: ResponseStyle): GenerativeModel {
        val systemInstructionText = buildSystemInstruction(language, responseStyle)
        return Firebase.ai.generativeModel(
            modelName = MODEL_NAME,
            generationConfig = generationConfig {
                temperature = 0.45f
                topP = 0.9f
            },
            systemInstruction = content { text(systemInstructionText) }
        )
    }

    override fun streamConsultation(
        history: List<ChatMessage>,
        userPrompt: String,
        language: AppLanguage,
        responseStyle: ResponseStyle
    ): Flow<String> = flow {
        val model = getModel(language, responseStyle)

        val chatHistory = history
            .filter { !it.isError && it.content.isNotBlank() }
            .takeLast(12)
            .map { msg ->
                content(role = if (msg.isUser) "user" else "model") {
                    text(msg.content)
                }
            }

        val chat = model.startChat(history = chatHistory)
        val accumulated = StringBuilder()

        try {
            withTimeout(GENERATION_TIMEOUT_MS) {
                chat.sendMessageStream(userPrompt).collect { chunk ->
                    val chunkText = chunk.text.orEmpty()
                    if (chunkText.isNotEmpty()) {
                        accumulated.append(chunkText)
                        emit(accumulated.toString())
                    }
                }
            }
            if (accumulated.isBlank()) {
                throw IllegalStateException(
                    when (language) {
                        AppLanguage.TG -> "Ёрдамчӣ ҷавоб сохта натавонист. Саволро равшантар нависед."
                        AppLanguage.RU -> "Ассистент не смог сформировать ответ. Попробуйте переформулировать вопрос."
                        AppLanguage.UZ_LATN -> "Yordamchi javob tuza olmadi. Savolni boshqacha yozib ko'ring."
                    }
                )
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "AI consultation timed out after ${GENERATION_TIMEOUT_MS}ms", e)
            throw e
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Firebase AI consultation failed", e)
            throw e
        }
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
}
