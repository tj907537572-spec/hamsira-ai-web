package com.example

import com.example.data.ai.BACKEND_CHAT_PROXY_URL
import com.example.data.ai.BackendConsultationException
import com.example.data.ai.HamshiraAiRepository
import com.example.data.model.AppLanguage
import com.example.data.model.ChatMessage
import com.example.data.model.ResponseStyle
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.net.UnknownHostException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HamshiraAiRepositoryTest {

    @Test
    fun `streamConsultation sends POST JSON with prompt key to backend proxy without API key and parses candidates text`() = runBlocking {
        var capturedRequest: Request? = null
        var capturedBodyText = ""

        val interceptor = Interceptor { chain ->
            val request = chain.request()
            capturedRequest = request
            val buffer = Buffer()
            request.body?.writeTo(buffer)
            capturedBodyText = buffer.readUtf8()

            val generateContentJson = """
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          { "text": "[МАСЛИҲАТ] Оби гарм нӯшед ва истироҳат кунед." }
                        ],
                        "role": "model"
                      }
                    }
                  ]
                }
            """.trimIndent()

            Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .addHeader("Content-Type", "application/json")
                .body(generateContentJson.toResponseBody("application/json".toMediaType()))
                .build()
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()
        val repository = HamshiraAiRepository(httpClient = client)

        val history = listOf(
            ChatMessage(sessionId = "s1", isUser = true, content = "Салом"),
            ChatMessage(sessionId = "s1", isUser = false, content = "Салом! Чӣ кӯмак расонам?")
        )

        val emissions = repository.streamConsultation(
            history = history,
            userPrompt = "Сарам каме дард мекунад",
            language = AppLanguage.TG,
            responseStyle = ResponseStyle.BALANCED
        ).toList()

        assertEquals(listOf("[МАСЛИҲАТ] Оби гарм нӯшед ва истироҳат кунед."), emissions)

        val req = requireNotNull(capturedRequest)
        assertEquals("POST", req.method)
        assertEquals(BACKEND_CHAT_PROXY_URL, req.url.toString())
        assertEquals("https://hamsira-ai-web.vercel.app/api/chat", req.url.toString())
        assertNull(req.header("x-goog-api-key"))
        assertNull(req.header("Authorization"))

        val payload = JSONObject(capturedBodyText)
        assertTrue(payload.has("prompt"))
        assertFalse(payload.has("contents"))
        val sentPrompt = payload.getString("prompt")
        assertTrue(sentPrompt.contains("Сарам каме дард мекунад"))
        assertTrue(sentPrompt.contains("Салом! Чӣ кӯмак расонам?"))
    }

    @Test
    fun `streamConsultation throws localized BackendConsultationException on HTTP 500 JSON error`() = runBlocking {
        val interceptor = Interceptor { chain ->
            Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(500)
                .message("Internal Server Error")
                .addHeader("Content-Type", "application/json")
                .body("""{"error":"Proxy overloaded"}""".toResponseBody("application/json".toMediaType()))
                .build()
        }

        val repository = HamshiraAiRepository(
            httpClient = OkHttpClient.Builder().addInterceptor(interceptor).build()
        )

        val failure = runCatching {
            repository.streamConsultation(
                history = emptyList(),
                userPrompt = "Ҳароратам баланд аст",
                language = AppLanguage.TG,
                responseStyle = ResponseStyle.BALANCED
            ).toList()
        }.exceptionOrNull()

        assertTrue(failure is BackendConsultationException)
        assertTrue(failure!!.message!!.contains("500"))
        assertTrue(failure.message!!.contains("Proxy overloaded"))
    }

    @Test
    fun `streamConsultation throws localized BackendConsultationException on network failure and invalid JSON`() = runBlocking {
        val offlineClient = OkHttpClient.Builder()
            .addInterceptor { throw UnknownHostException("Unable to resolve host") }
            .build()
        val offlineRepo = HamshiraAiRepository(httpClient = offlineClient)

        val networkError = runCatching {
            offlineRepo.streamConsultation(
                history = emptyList(),
                userPrompt = "Савол",
                language = AppLanguage.TG,
                responseStyle = ResponseStyle.CONCISE
            ).toList()
        }.exceptionOrNull()

        assertTrue(networkError is BackendConsultationException)
        assertTrue(networkError!!.message!!.contains("интернет", ignoreCase = true))

        val badJsonClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body("<html>bad gateway</html>".toResponseBody("text/html".toMediaType()))
                    .build()
            }
            .build()
        val badJsonRepo = HamshiraAiRepository(httpClient = badJsonClient)

        val jsonError = runCatching {
            badJsonRepo.streamConsultation(
                history = emptyList(),
                userPrompt = "Савол",
                language = AppLanguage.TG,
                responseStyle = ResponseStyle.CONCISE
            ).toList()
        }.exceptionOrNull()

        assertTrue(jsonError is BackendConsultationException)
        assertTrue(jsonError!!.message!!.isNotBlank())
    }
}
