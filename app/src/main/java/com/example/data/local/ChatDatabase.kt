package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.room.withTransaction
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.ChatMessage
import com.example.data.model.ConsultationSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val sessionId: String,
    val isUser: Boolean,
    val content: String,
    val timestamp: Long,
    val isError: Boolean = false
)

@Entity(tableName = "consultation_sessions")
data class ConsultationSessionEntity(
    @PrimaryKey val sessionId: String,
    val title: String,
    val lastMessagePreview: String,
    val messageCount: Int,
    val createdAt: Long,
    val updatedAt: Long
)

fun ChatMessageEntity.toDomain(): ChatMessage = ChatMessage(
    id = id,
    sessionId = sessionId,
    isUser = isUser,
    content = content,
    timestamp = timestamp,
    isError = isError
)

fun ConsultationSessionEntity.toDomain(): ConsultationSession = ConsultationSession(
    sessionId = sessionId,
    title = title,
    lastMessagePreview = lastMessagePreview,
    messageCount = messageCount,
    createdAt = createdAt,
    updatedAt = updatedAt
)

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC, id ASC")
    fun observeMessagesForSession(sessionId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC, id ASC")
    suspend fun getMessagesForSession(sessionId: String): List<ChatMessageEntity>

    @Query("SELECT COUNT(*) FROM chat_messages")
    fun observeTotalMessageCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesForSession(sessionId: String)

    @Query("DELETE FROM chat_messages")
    suspend fun clearAllMessages()

    @Query("SELECT sessionId FROM chat_messages ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestSessionId(): String?
}

@Dao
interface ConsultationSessionDao {
    @Query("SELECT * FROM consultation_sessions ORDER BY updatedAt DESC")
    fun observeAllSessions(): Flow<List<ConsultationSessionEntity>>

    @Query(
        """
        SELECT DISTINCT s.* FROM consultation_sessions s
        WHERE s.title LIKE '%' || :query || '%'
           OR s.lastMessagePreview LIKE '%' || :query || '%'
           OR EXISTS (
               SELECT 1 FROM chat_messages m
               WHERE m.sessionId = s.sessionId AND m.content LIKE '%' || :query || '%'
           )
        ORDER BY s.updatedAt DESC
        """
    )
    fun searchSessions(query: String): Flow<List<ConsultationSessionEntity>>

    @Query("SELECT * FROM consultation_sessions WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: String): ConsultationSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSession(session: ConsultationSessionEntity)

    @Query("DELETE FROM consultation_sessions WHERE sessionId = :sessionId")
    suspend fun deleteSessionById(sessionId: String)

    @Query("DELETE FROM consultation_sessions")
    suspend fun clearAllSessions()
}

@Database(
    entities = [ChatMessageEntity::class, ConsultationSessionEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun consultationSessionDao(): ConsultationSessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `consultation_sessions` (
                        `sessionId` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `lastMessagePreview` TEXT NOT NULL,
                        `messageCount` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`sessionId`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO `consultation_sessions`
                    (`sessionId`, `title`, `lastMessagePreview`, `messageCount`, `createdAt`, `updatedAt`)
                    SELECT
                        m.`sessionId`,
                        COALESCE(
                            (SELECT SUBSTR(u.`content`, 1, 64)
                             FROM `chat_messages` u
                             WHERE u.`sessionId` = m.`sessionId` AND u.`isUser` = 1
                             ORDER BY u.`timestamp` ASC, u.`id` ASC LIMIT 1),
                            'Машварат'
                        ),
                        COALESCE(
                            (SELECT SUBSTR(l.`content`, 1, 100)
                             FROM `chat_messages` l
                             WHERE l.`sessionId` = m.`sessionId`
                             ORDER BY l.`timestamp` DESC, l.`id` DESC LIMIT 1),
                            ''
                        ),
                        COUNT(*),
                        MIN(m.`timestamp`),
                        MAX(m.`timestamp`)
                    FROM `chat_messages` m
                    GROUP BY m.`sessionId`
                    """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hamshira_ai_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

class ChatRepository(private val database: AppDatabase) {
    private val messageDao = database.chatMessageDao()
    private val sessionDao = database.consultationSessionDao()

    fun observeSessionMessages(sessionId: String): Flow<List<ChatMessage>> =
        messageDao.observeMessagesForSession(sessionId).map { list -> list.map { it.toDomain() } }

    fun observeAllSessions(query: String = ""): Flow<List<ConsultationSession>> {
        val trimmed = query.trim()
        val source = if (trimmed.isEmpty()) {
            sessionDao.observeAllSessions()
        } else {
            sessionDao.searchSessions(trimmed)
        }
        return source.map { list -> list.map { it.toDomain() } }
    }

    fun observeTotalMessageCount(): Flow<Int> = messageDao.observeTotalMessageCount()

    suspend fun getSessionMessages(sessionId: String): List<ChatMessage> =
        messageDao.getMessagesForSession(sessionId).map { it.toDomain() }

    suspend fun addMessage(
        sessionId: String,
        isUser: Boolean,
        content: String,
        isError: Boolean = false
    ): Long {
        val now = System.currentTimeMillis()
        val preview = buildSnippet(content, 110)

        return database.withTransaction {
            val insertedId = messageDao.insertMessage(
                ChatMessageEntity(
                    sessionId = sessionId,
                    isUser = isUser,
                    content = content,
                    timestamp = now,
                    isError = isError
                )
            )

            val existing = sessionDao.getSessionById(sessionId)
            val updatedSession = if (existing == null) {
                val initialTitle = if (isUser) buildSnippet(content, 60) else "Машварат"
                ConsultationSessionEntity(
                    sessionId = sessionId,
                    title = initialTitle,
                    lastMessagePreview = preview,
                    messageCount = 1,
                    createdAt = now,
                    updatedAt = now
                )
            } else {
                val resolvedTitle = if (existing.title == "Машварат" && isUser) {
                    buildSnippet(content, 60)
                } else {
                    existing.title
                }
                existing.copy(
                    title = resolvedTitle,
                    lastMessagePreview = preview,
                    messageCount = existing.messageCount + 1,
                    updatedAt = now
                )
            }
            sessionDao.upsertSession(updatedSession)
            insertedId
        }
    }

    suspend fun getLatestSessionId(): String? = messageDao.getLatestSessionId()

    suspend fun deleteSession(sessionId: String) {
        database.withTransaction {
            messageDao.deleteMessagesForSession(sessionId)
            sessionDao.deleteSessionById(sessionId)
        }
    }

    suspend fun clearAllHistory() {
        database.withTransaction {
            messageDao.clearAllMessages()
            sessionDao.clearAllSessions()
        }
    }

    private fun buildSnippet(raw: String, maxLength: Int): String {
        val clean = raw
            .replace(Regex("\\*\\*|###|##|#"), "")
            .replace(Regex("\\[(МАСЛИҲАТ|ДИҚҚАТ|ФАВРАН БА ДУХТУР|СОВЕТ|ВНИМАНИЕ|СРОЧНО К ВРАЧУ)]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
        return if (clean.length <= maxLength) clean else clean.take(maxLength - 1).trimEnd() + "…"
    }
}
