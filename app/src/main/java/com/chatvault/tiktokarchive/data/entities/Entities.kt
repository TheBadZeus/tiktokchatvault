package com.chatvault.tiktokarchive.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.chatvault.tiktokarchive.domain.model.AttachmentStatus
import com.chatvault.tiktokarchive.domain.model.CaptureMethod
import com.chatvault.tiktokarchive.domain.model.Direction
import com.chatvault.tiktokarchive.domain.model.EventType
import com.chatvault.tiktokarchive.domain.model.MessageType
import com.chatvault.tiktokarchive.domain.model.VerificationStatus

@Entity(tableName = "contacts", indices = [Index(value = ["username"])])
data class Contact(
    @PrimaryKey val id: String,
    val username: String?,
    val displayName: String?,
    val avatarUri: String?,
)

@Entity(
    tableName = "conversations",
    indices = [Index(value = ["lastMessageAt"]), Index(value = ["contactId"])],
)
data class Conversation(
    @PrimaryKey val id: String,
    val contactId: String?,
    val title: String,
    val lastMessagePreview: String?,
    /** Ordering value of the newest message (original time if the source gave one, else capture time). */
    val lastMessageAt: Long?,
    /** "Unread" means not yet opened inside this archive app. */
    val unreadCount: Int,
    val isDemo: Boolean,
    val createdAt: Long,
)

@Entity(
    tableName = "archived_messages",
    foreignKeys = [
        ForeignKey(
            entity = Conversation::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["conversationId", "sortTimestamp"]),
        Index(value = ["senderId"]),
        Index(value = ["captureTimestamp"]),
        Index(value = ["conversationId", "sourceMessageId"], unique = true),
        Index(value = ["conversationId", "dedupKey"], unique = true),
    ],
)
data class ArchivedMessage(
    @PrimaryKey val localId: String,
    /** ID assigned by the source (TikTok), only when the source supplied one. */
    val sourceMessageId: String?,
    /** Key used to drop duplicate captures (e.g. repeated notifications). */
    val dedupKey: String?,
    val conversationId: String,
    val senderId: String?,
    val senderUsername: String?,
    val direction: Direction,
    val messageType: MessageType,
    val content: String?,
    /** Time reported by the source. Null when the source did not provide one. Never filled in by guessing. */
    val originalTimestamp: Long?,
    /** Time this app saved the record. */
    val captureTimestamp: Long,
    /** Used ONLY for ordering: originalTimestamp if present, otherwise captureTimestamp. Not displayed as a fact. */
    val sortTimestamp: Long,
    val captureMethod: CaptureMethod,
    val captureSource: String,
    /** SHA-256 over the canonical form of the immutable fields, computed when the record was saved. */
    val integrityHash: String,
    val verificationStatus: VerificationStatus,
    val lastVerifiedAt: Long?,
    /** User annotation ("saved"). Not part of the hash. */
    val isStarred: Boolean,
)

@Entity(
    tableName = "message_attachments",
    foreignKeys = [
        ForeignKey(
            entity = ArchivedMessage::class,
            parentColumns = ["localId"],
            childColumns = ["messageLocalId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["messageLocalId"])],
)
data class MessageAttachment(
    @PrimaryKey val id: String,
    val messageLocalId: String,
    val kind: MessageType,
    val fileName: String?,
    val mimeType: String?,
    val sizeBytes: Long?,
    val localUri: String?,
    val sha256: String?,
    val sourceReference: String?,
    val status: AttachmentStatus,
)

/** Doubles as the audit log: rows are only ever inserted, never updated (see CaptureEventDao). */
@Entity(tableName = "capture_events", indices = [Index(value = ["timestamp"])])
data class CaptureEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val type: EventType,
    val success: Boolean,
    /** Never contains message text. */
    val detail: String?,
    val itemCount: Int,
)

@Entity(tableName = "evidence_exports")
data class EvidenceExport(
    @PrimaryKey val id: String,
    val createdAt: Long,
    val format: String,
    val conversationId: String?,
    val rangeStart: Long?,
    val rangeEnd: Long?,
    val recordCount: Int,
    val missingAttachments: Int,
    val verifiedIntact: Int,
    val modified: Int,
    val manifestSha256: String?,
    val destinationUri: String?,
)

@Entity(tableName = "app_settings")
data class AppSetting(
    @PrimaryKey val settingKey: String,
    val settingValue: String,
)
