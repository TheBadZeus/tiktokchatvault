package com.chatvault.tiktokarchive.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.room.withTransaction
import com.chatvault.tiktokarchive.data.dao.InboxRow
import com.chatvault.tiktokarchive.data.database.ChatVaultDatabase
import com.chatvault.tiktokarchive.data.demo.DemoData
import com.chatvault.tiktokarchive.data.entities.ArchivedMessage
import com.chatvault.tiktokarchive.data.entities.CaptureEvent
import com.chatvault.tiktokarchive.data.entities.Conversation
import com.chatvault.tiktokarchive.data.entities.MessageAttachment
import com.chatvault.tiktokarchive.domain.model.EventType
import com.chatvault.tiktokarchive.domain.model.MessageType
import com.chatvault.tiktokarchive.domain.model.VerificationStatus
import com.chatvault.tiktokarchive.evidence.IntegrityHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/** Escapes LIKE wildcards so user input is matched literally. The SQL uses '!' as the ESCAPE character. */
internal fun escapeLike(raw: String): String = raw.replace("!", "!!").replace("%", "!%").replace("_", "!_")

class ArchiveRepository(
    private val db: ChatVaultDatabase,
    private val clock: () -> Long = { System.currentTimeMillis() },
) {
    // ---------- reading ----------
    fun inbox(query: String): Flow<List<InboxRow>> = db.conversationDao().inbox(escapeLike(query.trim()))

    fun conversation(id: String): Flow<Conversation?> = db.conversationDao().observe(id)

    fun messages(
        conversationId: String,
        query: String,
        senderId: String? = null,
        from: Long? = null,
        to: Long? = null,
    ): Flow<PagingData<ArchivedMessage>> =
        Pager(PagingConfig(pageSize = 40, initialLoadSize = 80, enablePlaceholders = false)) {
            db.messageDao().pagingSource(conversationId, escapeLike(query.trim()), senderId, from, to)
        }.flow

    suspend fun message(localId: String): ArchivedMessage? = db.messageDao().get(localId)

    suspend fun attachments(localId: String): List<MessageAttachment> = db.attachmentDao().forMessage(localId)

    fun messageCount(): Flow<Int> = db.messageDao().observeCount()
    fun conversationCount(): Flow<Int> = db.conversationDao().observeCount()
    fun recentEvents(limit: Int): Flow<List<CaptureEvent>> = db.captureEventDao().recent(limit)

    // ---------- small writes ----------
    suspend fun markRead(conversationId: String) = db.conversationDao().markRead(conversationId)

    suspend fun setStarred(localId: String, starred: Boolean) = db.messageDao().setStarred(localId, starred)

    // ---------- archiving ----------
    /** Seals the record with its SHA-256 hash and inserts it. Returns false if it was a duplicate. */
    suspend fun archive(message: ArchivedMessage, attachments: List<MessageAttachment> = emptyList()): Boolean {
        val sealed = message.copy(
            integrityHash = IntegrityHasher.hashOf(message),
            verificationStatus = VerificationStatus.UNVERIFIED,
            lastVerifiedAt = null,
        )
        if (db.messageDao().insert(sealed) == -1L) return false
        if (attachments.isNotEmpty()) db.attachmentDao().insertAll(attachments)
        return true
    }

    private suspend fun refreshSummary(conversationId: String) {
        val newest = db.messageDao().newest(conversationId)
        db.conversationDao().updateSummary(conversationId, newest?.let(::previewOf), newest?.sortTimestamp)
    }

    private fun previewOf(m: ArchivedMessage): String =
        m.content?.takeIf { it.isNotBlank() }?.take(120) ?: when (m.messageType) {
            MessageType.IMAGE -> "[Image]"
            MessageType.VIDEO -> "[Video]"
            MessageType.ATTACHMENT -> "[Attachment]"
            else -> "[No text]"
        }

    private suspend fun logEvent(type: EventType, detail: String?, count: Int, success: Boolean = true) {
        db.captureEventDao().insert(CaptureEvent(timestamp = clock(), type = type, success = success, detail = detail, itemCount = count))
    }

    // ---------- verification ----------
    /** Recomputes the hash for real and stores the result. Never reports INTACT without checking. */
    suspend fun verifyMessage(localId: String): VerificationStatus {
        val msg = db.messageDao().get(localId) ?: return VerificationStatus.UNVERIFIED
        val status = IntegrityHasher.verify(msg)
        db.withTransaction {
            db.messageDao().setVerification(localId, status.name, clock())
            logEvent(EventType.VERIFY, "record $localId checked: ${status.name}", 1, success = status == VerificationStatus.INTACT)
        }
        return status
    }

    // ---------- development helpers ----------
    suspend fun loadDemoData(): Int = db.withTransaction {
        val seed = DemoData.build(clock())
        db.contactDao().insertAll(seed.contacts)
        db.conversationDao().insertAll(seed.conversations)
        var inserted = 0
        seed.messages.forEach { if (archive(it)) inserted++ }
        db.attachmentDao().insertAll(seed.attachments)
        seed.conversations.forEach { refreshSummary(it.id) }
        logEvent(EventType.DEMO_LOAD, "Inserted $inserted demo messages", inserted)
        inserted
    }

    suspend fun deleteDemoData() = db.withTransaction {
        db.conversationDao().deleteDemo() // cascades to demo messages and attachments
        db.contactDao().deleteDemo()
        logEvent(EventType.DELETE, "Removed demo data", 0)
    }

    /** Edits a demo message's text without updating its hash, so verification can be seen catching it. */
    suspend fun tamperWithDemoMessage(): Boolean {
        val id = "demo-maya-1"
        if (db.messageDao().get(id) == null) return false
        db.withTransaction {
            db.messageDao().overwriteContentWithoutRehash(id, "[edited outside the app]")
            logEvent(EventType.TAMPER_TEST, "Demo record edited without updating its hash", 1)
        }
        return true
    }

    // ---------- deletion ----------
    /** Deletes everything, including the audit log, then records that a wipe happened. */
    suspend fun deleteAllData() = withContext(Dispatchers.IO) {
        db.clearAllTables()
        runCatching { db.openHelper.writableDatabase.execSQL("VACUUM") }
        logEvent(EventType.DELETE, "All archived data deleted by the user", 0)
    }
}
