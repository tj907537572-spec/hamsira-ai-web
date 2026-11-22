package com.example

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.ai.NurseAiClient
import com.example.data.local.AppDatabase
import com.example.data.local.ChatRepository
import com.example.data.model.AppLanguage
import com.example.data.model.ChatMessage
import com.example.data.model.FirstAidCatalog
import com.example.data.model.FirstAidMediaFilter
import com.example.data.model.MediaAttachment
import com.example.data.model.ResponseStyle
import com.example.ui.HamshiraViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FirstAidCatalogTest {

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
    fun `first aid catalog includes all 6 required emergencies with images and video scenes`() {
        val topics = FirstAidCatalog.topics(AppLanguage.TG)
        val ids = topics.map { it.id }.toSet()

        assertEquals(setOf("burns", "bleeding", "choking", "fainting", "fever", "wounds"), ids)
        topics.forEach { topic ->
            assertTrue("Missing drawable for ${topic.id}", topic.imageRes != 0)
            assertTrue("Missing video scenes for ${topic.id}", topic.videoScenes.size >= 4)
            assertTrue("Missing doSteps for ${topic.id}", topic.doSteps.isNotEmpty())
            assertTrue("Missing doNotSteps for ${topic.id}", topic.doNotSteps.isNotEmpty())
            assertTrue("Missing redFlags for ${topic.id}", topic.redFlags.isNotEmpty())
            assertTrue("Missing officialVideoUrl for ${topic.id}", topic.officialVideoUrl.startsWith("https://"))
        }
    }

    @Test
    fun `viewModel filters first aid topics and opens topic in video or photo mode`() = runBlocking {
        val noopAi = object : NurseAiClient {
            override fun streamConsultation(
                history: List<ChatMessage>,
                userPrompt: String,
                language: AppLanguage,
                responseStyle: ResponseStyle,
                attachment: MediaAttachment?
            ): Flow<String> = emptyFlow()
        }

        val viewModel = HamshiraViewModel(
            application = application,
            chatRepository = chatRepository,
            aiClient = noopAi
        )

        viewModel.onFirstAidSearchChanged("Сӯхтан")
        viewModel.selectFirstAidFilter(FirstAidMediaFilter.VIDEOS)

        val filteredState = withTimeout(5_000L) {
            viewModel.uiState.first {
                it.firstAidFilter == FirstAidMediaFilter.VIDEOS &&
                    it.firstAidSearchQuery == "Сӯхтан" &&
                    it.firstAidTopics.size == 1
            }
        }
        assertEquals("burns", filteredState.firstAidTopics.first().id)

        val burnsTopic = filteredState.firstAidTopics.first()
        viewModel.openFirstAidTopic(burnsTopic, startInVideoMode = false)
        val openedState = withTimeout(5_000L) {
            viewModel.uiState.first { it.selectedFirstAidTopic?.id == "burns" }
        }
        assertNotNull(openedState.selectedFirstAidTopic)
        assertFalse(openedState.firstAidStartInVideoMode)
    }
}
