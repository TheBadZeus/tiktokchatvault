package com.chatvault.tiktokarchive.data.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.chatvault.tiktokarchive.data.entities.AppSetting
import com.chatvault.tiktokarchive.data.entities.ArchivedMessage
import com.chatvault.tiktokarchive.data.entities.CaptureEvent
import com.chatvault.tiktokarchive.data.entities.Contact
import com.chatvault.tiktokarchive.data.entities.Conversation
import com.chatvault.tiktokarchive.data.entities.EvidenceExport
import com.chatvault.tiktokarchive.data.entities.MessageAttachment
import kotlinx.coroutines.flow.Flow

/** One line of the inbox list. Column aliases in the query must match these names. */
data class InboxRow(
    val id: String,
    val title: String,
    val lastMessagePreview: String?,
    val lastMessageAt: Long?,
    val unreadCount: Int,
    val isDemo: Boolean,
    val hasStarred: Boolean,
)

@Dao
interface ContactDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<Contact>)

    @Query("DELETE FROM contacts WHERE id LIKE 'demo-%'")
    suspend fun deleteDemo()
}

@Dao
interface ConversationDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<Conversation>)

    @Query("SELECT * FROM conversations WHERE id = :id")
    fun observe(id: String): Flow<Conversation?>

    /** :query is already escaped for LIKE with '!' as the escape character (see escapeLike). */
    @Query(
        """
        SELECT c.id AS id, c.title AS title, c.lastMessagePreview AS lastMessagePreview,
               c.lastMessageAt AS lastMessageAt, c.unreadCount AS unreadCount, c.isDemo AS isDemo,
               EXISTS(SELECT 1 FROM archived_messages s WHERE s.conversationId = c.id AND s.isStarred = 1) AS hasStarred
        FROM conversations c
        WHERE :query = ''
           OR c.title LIKE '%' || :query || '%' ESCAPE '!'
           OR EXISTS(SELECT 1 FROM archived_messages m
                     WHERE m.conversationId = c.id AND m.content LIKE '%' || :query || '%' ESCAPE '!')
        ORDER BY c.lastMessageAt DESC
        """
    )
    fun inbox(query: String): Flow<List<InboxRow>>

    @Query("UPDATE conversations SET lastMessagePreview = :preview, lastMessageAt = :at WHERE id = :id")
    suspend fun updateSummary(id: String, preview: String?, at: Long?)

    @Query("UPDATE conversations SET unreadCount = 0 WHERE id = :id")
    suspend fun markRead(id: String)

    @Query("DELETE FROM conversations WHERE isDemo = 1")
    suspend fun deleteDemo()

    @Query("SELECT COUNT(*) FROM conversations")
    fun observeCount(): Flow<Int>
}

@Dao
interface MessageDao {
    /** Returns -1 when the row was ignored because it duplicates an existing one. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(message: ArchivedMessage): Long

    @Query(
        """
        SELECT * FROM archived_messages
        WHERE conversationId = :conversationId
          AND (:query = '' OR content LIKE '%' || :query || '%' ESCAPE '!')
          AND (:senderId IS NULL OR senderId = :senderId)
          AND (:fromTs IS NULL OR sortTimestamp >= :fromTs)
          AND (:toTs IS NULL OR sortTimestamp <= :toTs)
        ORDER BY sortTimestamp DESC, localId DESC
        """
    )
    fun pagingSource(
        conversationId: String,
        query: String,
        senderId: String?,
        fromTs: Long?,
        toTs: Long?,
    ): PagingSource<Int, ArchivedMessage>

    @Query("SELECT * FROM archived_messages WHERE localId = :id")
    suspend fun get(id: String): ArchivedMessage?

    @Query("SELECT * FROM archived_messages WHERE conversationId = :conversationId ORDER BY sortTimestamp DESC, localId DESC LIMIT 1")
    suspend fun newest(conversationId: String): ArchivedMessage?

    @Query("UPDATE archived_messages SET isStarred = :starred WHERE localId = :id")
    suspend fun setStarred(id: String, starred: Boolean)

    @Query("UPDATE archived_messages SET verificationStatus = :status, lastVerifiedAt = :at WHERE localId = :id")
    suspend fun setVerification(id: String, status: String, at: Long)

    @Query("SELECT COUNT(*) FROM archived_messages")
    fun observeCount(): Flow<Int>

    /** Used only by the demo "tamper test": changes content WITHOUT recomputing the hash. */
    @Query("UPDATE archived_messages SET content = :content WHERE localId = :id")
    suspend fun overwriteContentWithoutRehash(id: String, content: String)
}

@Dao
interface AttachmentDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<MessageAttachment>)

    @Query("SELECT * FROM message_attachments WHERE messageLocalId = :messageLocalId")
    suspend fun forMessage(messageLocalId: String): List<MessageAttachment>
}

/** Append-only by design: there is intentionally no update or delete method. */
@Dao
interface CaptureEventDao {
    @Insert
    suspend fun insert(event: CaptureEvent)

    @Query("SELECT * FROM capture_events ORDER BY timestamp DESC, id DESC LIMIT :limit")
    fun recent(limit: Int): Flow<List<CaptureEvent>>
}

@Dao
interface EvidenceExportDao {
    @Insert
    suspend fun insert(item: EvidenceExport)

    @Query("SELECT * FROM evidence_exports ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<EvidenceExport>>
}

@Dao
interface AppSettingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(item: AppSetting)

    @Query("SELECT settingValue FROM app_settings WHERE settingKey = :key")
    suspend fun get(key: String): String?
}
