package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.ChatRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ChatDatabaseTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: ChatRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ChatRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `saves multiple consultation sessions and searches by content`() = runTest {
        repository.addMessage("session-1", isUser = true, content = "Ҳарорати 38.5°C дар хона")
        repository.addMessage("session-1", isUser = false, content = "[МАСЛИҲАТ] Оби гарм нӯшед ва истироҳат кунед.")

        repository.addMessage("session-2", isUser = true, content = "Фишори хун 150/95")
        repository.addMessage("session-2", isUser = false, content = "Фишорро дубора пас аз 5 дақиқа чен кунед.")

        val sessions = repository.observeAllSessions().first()
        assertEquals(2, sessions.size)
        assertEquals(2, sessions.first { it.sessionId == "session-1" }.messageCount)

        val searchByBody = repository.observeAllSessions("истироҳат").first()
        assertEquals(1, searchByBody.size)
        assertEquals("session-1", searchByBody.first().sessionId)

        val session1Messages = repository.getSessionMessages("session-1")
        assertEquals(2, session1Messages.size)
        assertTrue(session1Messages.first().isUser)
    }

    @Test
    fun `deleteSession removes only target consultation`() = runTest {
        repository.addMessage("session-a", isUser = true, content = "Саволи якум")
        repository.addMessage("session-b", isUser = true, content = "Саволи дуюм")

        repository.deleteSession("session-a")

        val sessions = repository.observeAllSessions().first()
        assertEquals(1, sessions.size)
        assertEquals("session-b", sessions.first().sessionId)
        assertEquals(0, repository.getSessionMessages("session-a").size)
    }
}
