package com.example

import com.example.data.ai.BACKEND_CHAT_PROXY_URL
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HamshiraAiRepositoryTest {

    @Test
    fun `streamConsultation sends POST JSON to backend proxy without API key and parses generateContent response`() = runBlocking {
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
        assertTrue(payload.has("systemInstruction"))
        assertTrue(payload.has("contents"))
        val contents = payload.getJSONArray("contents")
        assertEquals(3, contents.length())
        assertEquals(
            "Сарам каме дард мекунад",
            contents.getJSONObject(2).getJSONArray("parts").getJSONObject(0).getString("text")
        )
    }

    @Test
    fun `extractTextFromGenerateContentJson joins multi-part generateContent responses`() {
        val repository = HamshiraAiRepository()
        val json = """
            {
              "candidates": [
                {
                  "content": {
                    "parts": [
                      { "text": "Қисми якум. " },
                      { "text": "Қисми дуюм." }
                    ]
                  }
                }
              ]
            }
        """.trimIndent()

        val result = repository.extractTextFromGenerateContentJson(json)
        assertEquals("Қисми якум. Қисми дуюм.", result)
    }
}
