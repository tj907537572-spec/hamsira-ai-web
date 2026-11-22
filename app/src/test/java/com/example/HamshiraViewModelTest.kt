package com.example

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.ai.NurseAiClient
import com.example.data.local.AppDatabase
import com.example.data.local.ChatRepository
import com.example.data.model.AppLanguage
import com.example.data.model.ChatMessage
import com.example.data.model.MediaAttachment
import com.example.data.model.ResponseStyle
import com.example.data.model.stringsFor
import com.example.ui.ConsultationState
import com.example.ui.HamshiraViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HamshiraViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var application: Application
    private lateinit var database: AppDatabase
    private lateinit var chatRepository: ChatRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        application = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(application, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        chatRepository = ChatRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
        Dispatchers.resetMain()
    }

    @Test
    fun `sendCurrentInput processes AI response and persists both messages in Room`() = runBlocking {
        val fakeAi = object : NurseAiClient {
            override fun streamConsultation(
                history: List<ChatMessage>,
                userPrompt: String,
                language: AppLanguage,
                responseStyle: ResponseStyle,
                attachment: MediaAttachment?
            ): Flow<String> = flow {
                emit("[МАСЛИҲАТ] ")
                emit("[МАСЛИҲАТ] Оби гарм нӯшед ва ҳароратро назорат кунед.")
            }
        }

        val viewModel = HamshiraViewModel(
            application = application,
            chatRepository = chatRepository,
            aiClient = fakeAi
        )

        viewModel.onInputChanged("Ҳароратам 38.2°C аст")
        viewModel.sendCurrentInput()

        val state = withTimeout(5_000L) {
            viewModel.uiState.first {
                it.messages.size == 2 && it.consultationState == ConsultationState.Idle
            }
        }

        assertTrue(state.messages[0].isUser)
        assertEquals("Ҳароратам 38.2°C аст", state.messages[0].content)
        assertFalse(state.messages[1].isUser)
        assertEquals(
            "[МАСЛИҲАТ] Оби гарм нӯшед ва ҳароратро назорат кунед.",
            state.messages[1].content
        )

        val savedSessions = withTimeout(5_000L) {
            chatRepository.observeAllSessions().first { it.isNotEmpty() && it.first().messageCount == 2 }
        }
        assertEquals(1, savedSessions.size)
        assertEquals(2, savedSessions.first().messageCount)
    }

    @Test
    fun `sendCurrentInput with attachment and empty text uses default prompt and persists media`() = runBlocking {
        var receivedAttachment: MediaAttachment? = null
        val fakeAi = object : NurseAiClient {
            override fun streamConsultation(
                history: List<ChatMessage>,
                userPrompt: String,
                language: AppLanguage,
                responseStyle: ResponseStyle,
                attachment: MediaAttachment?
            ): Flow<String> = flow {
                receivedAttachment = attachment
                emit("Дар акс сурхии сабук дида мешавад. Лутфан ба духтур нишон диҳед.")
            }
        }

        val viewModel = HamshiraViewModel(
            application = application,
            chatRepository = chatRepository,
            aiClient = fakeAi
        )

        val media = MediaAttachment(
            uriString = "file:///data/user/0/com.example/files/attachments/sample.jpg",
            mimeType = "image/jpeg",
            isVideo = false,
            displayName = "sample.jpg",
            sizeBytes = 42_000L
        )
        viewModel.attachResolvedMedia(media)
        viewModel.sendCurrentInput()

        val state = withTimeout(5_000L) {
            viewModel.uiState.first {
                it.messages.size == 2 && it.consultationState == ConsultationState.Idle
            }
        }

        assertNull(state.pendingAttachment)
        assertEquals(media, receivedAttachment)
        assertEquals(stringsFor(AppLanguage.TG).defaultMediaPrompt, state.messages[0].content)
        assertEquals(media.uriString, state.messages[0].attachmentUri)
        assertEquals("image/jpeg", state.messages[0].attachmentMimeType)
        assertFalse(state.messages[0].attachmentIsVideo)
    }

    @Test
    fun `retry after AI failure does not duplicate user message in Room`() = runBlocking {
        var shouldFail = true
        val flakyAi = object : NurseAiClient {
            override fun streamConsultation(
                history: List<ChatMessage>,
                userPrompt: String,
                language: AppLanguage,
                responseStyle: ResponseStyle,
                attachment: MediaAttachment?
            ): Flow<String> = flow {
                if (shouldFail) {
                    throw IllegalStateException("Temporary network error")
                }
                emit("Ҷавоби барқароршуда аз Ҳамшира AI.")
            }
        }

        val viewModel = HamshiraViewModel(
            application = application,
            chatRepository = chatRepository,
            aiClient = flakyAi
        )

        viewModel.sendQuickPrompt("Фишори хунро чӣ тавр чен кунем?")

        val errorState = withTimeout(5_000L) {
            viewModel.uiState.first {
                it.consultationState is ConsultationState.Error && it.messages.size == 1
            }
        }
        assertTrue(errorState.consultationState is ConsultationState.Error)

        shouldFail = false
        viewModel.retryFailedPrompt()

        val recoveredState = withTimeout(5_000L) {
            viewModel.uiState.first {
                it.messages.size == 2 && it.consultationState == ConsultationState.Idle
            }
        }

        assertTrue(recoveredState.messages[0].isUser)
        assertFalse(recoveredState.messages[1].isUser)
        assertEquals("Ҷавоби барқароршуда аз Ҳамшира AI.", recoveredState.messages[1].content)
    }
}
