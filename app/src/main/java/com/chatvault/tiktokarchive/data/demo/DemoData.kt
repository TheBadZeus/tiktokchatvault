package com.chatvault.tiktokarchive.data.demo

import com.chatvault.tiktokarchive.data.entities.ArchivedMessage
import com.chatvault.tiktokarchive.data.entities.Contact
import com.chatvault.tiktokarchive.data.entities.Conversation
import com.chatvault.tiktokarchive.data.entities.MessageAttachment
import com.chatvault.tiktokarchive.domain.model.AttachmentStatus
import com.chatvault.tiktokarchive.domain.model.CaptureMethod
import com.chatvault.tiktokarchive.domain.model.Direction
import com.chatvault.tiktokarchive.domain.model.MessageType
import com.chatvault.tiktokarchive.domain.model.VerificationStatus
import kotlin.random.Random

/**
 * Fake data for development only. Everything here is invented, uses captureMethod = DEMO, and is labelled as demo
 * in the UI so it can never be mistaken for captured evidence. IDs are deterministic so loading twice adds nothing.
 */
internal object DemoData {
    class Seed(
        val contacts: List<Contact>,
        val conversations: List<Conversation>,
        val messages: List<ArchivedMessage>,
        val attachments: List<MessageAttachment>,
    )

    private class Person(
        val key: String,
        val username: String,
        val count: Int,
        val unread: Int,
        /** false = simulates a record whose source gave no timestamp (like a bare notification). */
        val hasOriginalTs: Boolean = true,
        val incomingOnly: Boolean = false,
    )

    private val people = listOf(
        Person("maya", "maya.demo", 42, 2),
        Person("jordan", "jordan_demo", 120, 0),
        Person("studio", "lee.studio.demo", 18, 1, hasOriginalTs = false, incomingOnly = true),
        Person("crew", "weekend.crew.demo", 60, 0),
        Person("longthread", "long_thread_demo", 2500, 0),
    )

    private val incomingLines = listOf(
        "hey, are you free later?", "did you see that video?", "lol that's so funny", "sending the details now",
        "call me when you get this", "ok sounds good", "can you send me the link again?", "I'll be there in 10",
        "thanks!", "no worries", "that was actually amazing", "wait, what time is it again?",
    )
    private val outgoingLines = listOf(
        "yeah give me a sec", "sure, what time?", "haha yes", "on my way", "I'll check and let you know",
        "sounds good to me", "can't right now, tonight?", "got it, thanks", "send it over", "perfect",
    )

    fun build(now: Long): Seed {
        val rnd = Random(42)
        val contacts = mutableListOf<Contact>()
        val conversations = mutableListOf<Conversation>()
        val messages = mutableListOf<ArchivedMessage>()
        val attachments = mutableListOf<MessageAttachment>()
        val minute = 60_000L

        for (p in people) {
            val contactId = "demo-contact-${p.key}"
            val convId = "demo-conv-${p.key}"
            contacts += Contact(contactId, p.username, null, null)
            conversations += Conversation(convId, contactId, p.username, null, null, p.unread, true, now)

            for (i in 0 until p.count) {
                val ts = now - (p.count - i) * 40L * minute + rnd.nextInt(0, 20 * 60_000)
                val outgoing = !p.incomingOnly && rnd.nextInt(100) < 45
                val isMedia = i % 37 == 36
                val localId = "demo-${p.key}-$i"
                messages += ArchivedMessage(
                    localId = localId,
                    sourceMessageId = null,
                    dedupKey = "demo-$i",
                    conversationId = convId,
                    senderId = if (outgoing) "demo-self" else contactId,
                    senderUsername = if (outgoing) "me.demo" else p.username,
                    direction = if (outgoing) Direction.OUTGOING else Direction.INCOMING,
                    messageType = if (isMedia) MessageType.IMAGE else MessageType.TEXT,
                    content = if (isMedia) null else (if (outgoing) outgoingLines else incomingLines).random(rnd),
                    originalTimestamp = if (p.hasOriginalTs) ts else null,
                    captureTimestamp = if (p.hasOriginalTs) now else ts,
                    sortTimestamp = ts,
                    captureMethod = CaptureMethod.DEMO,
                    captureSource = if (p.hasOriginalTs) "Built-in demo generator"
                    else "Built-in demo generator (simulates a record with no source timestamp)",
                    integrityHash = "", // sealed by the repository
                    verificationStatus = VerificationStatus.UNVERIFIED,
                    lastVerifiedAt = null,
                    isStarred = i % 53 == 0,
                )
                if (isMedia) {
                    attachments += MessageAttachment(
                        id = "demo-att-${p.key}-$i",
                        messageLocalId = localId,
                        kind = MessageType.IMAGE,
                        fileName = null, mimeType = null, sizeBytes = null, localUri = null, sha256 = null,
                        sourceReference = null,
                        status = AttachmentStatus.NOT_CAPTURED,
                    )
                }
            }
        }
        return Seed(contacts, conversations, messages, attachments)
    }
}
