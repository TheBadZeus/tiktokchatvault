package com.chatvault.tiktokarchive.evidence

import com.chatvault.tiktokarchive.data.entities.ArchivedMessage
import com.chatvault.tiktokarchive.domain.model.VerificationStatus
import java.security.MessageDigest

/**
 * SHA-256 integrity hashing for archived records.
 *
 * What a matching hash means: the stored fields are identical to what this app stored when it saved the record.
 * What it does NOT mean: that the message is authentic, that TikTok or the sender produced it, or that it would be
 * accepted as evidence anywhere. Anyone with access to the database could change a record and its hash together.
 */
object IntegrityHasher {
    const val SCHEME = "chatvault-v1"

    fun sha256Hex(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }

    fun sha256Hex(text: String): String = sha256Hex(text.toByteArray(Charsets.UTF_8))

    /** null -> "N"; value -> "S<length>:<value>", so a null can never collide with the text "N". */
    private fun enc(v: Any?): String = if (v == null) "N" else v.toString().let { "S${it.length}:$it" }

    /**
     * Hashed: the fields that describe what was captured, when, and how.
     * Not hashed: sortTimestamp (derived), isStarred / verificationStatus / lastVerifiedAt (change over time), integrityHash itself.
     */
    fun canonicalForm(m: ArchivedMessage): String = listOf(
        SCHEME,
        m.localId,
        m.sourceMessageId,
        m.dedupKey,
        m.conversationId,
        m.senderId,
        m.senderUsername,
        m.direction.name,
        m.messageType.name,
        m.content,
        m.originalTimestamp,
        m.captureTimestamp,
        m.captureMethod.name,
        m.captureSource,
    ).joinToString("|") { enc(it) }

    fun hashOf(m: ArchivedMessage): String = sha256Hex(canonicalForm(m))

    fun verify(m: ArchivedMessage): VerificationStatus =
        if (m.integrityHash.equals(hashOf(m), ignoreCase = true)) VerificationStatus.INTACT else VerificationStatus.MODIFIED
}
