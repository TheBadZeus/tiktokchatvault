package com.chatvault.tiktokarchive.evidence

import com.chatvault.tiktokarchive.data.entities.ArchivedMessage
import com.chatvault.tiktokarchive.domain.model.CaptureMethod
import com.chatvault.tiktokarchive.domain.model.Direction
import com.chatvault.tiktokarchive.domain.model.MessageType
import com.chatvault.tiktokarchive.domain.model.VerificationStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class IntegrityHasherTest {
    private fun sample(content: String? = "hello") = ArchivedMessage(
        localId = "m1", sourceMessageId = null, dedupKey = "k1", conversationId = "c1",
        senderId = "u1", senderUsername = "someone", direction = Direction.INCOMING,
        messageType = MessageType.TEXT, content = content,
        originalTimestamp = 1_700_000_000_000, captureTimestamp = 1_700_000_100_000, sortTimestamp = 1_700_000_000_000,
        captureMethod = CaptureMethod.DEMO, captureSource = "test",
        integrityHash = "", verificationStatus = VerificationStatus.UNVERIFIED, lastVerifiedAt = null, isStarred = false,
    )

    private fun sealed(m: ArchivedMessage) = m.copy(integrityHash = IntegrityHasher.hashOf(m))

    @Test
    fun sha256_matchesKnownVector() {
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", IntegrityHasher.sha256Hex("abc"))
    }

    @Test
    fun untouchedRecord_verifiesAsIntact() {
        assertEquals(VerificationStatus.INTACT, IntegrityHasher.verify(sealed(sample())))
    }

    @Test
    fun editedContent_isDetectedAsModified() {
        val edited = sealed(sample()).copy(content = "hello!")
        assertEquals(VerificationStatus.MODIFIED, IntegrityHasher.verify(edited))
    }

    @Test
    fun editedTimestamp_isDetectedAsModified() {
        val edited = sealed(sample()).copy(originalTimestamp = 1_700_000_000_001)
        assertEquals(VerificationStatus.MODIFIED, IntegrityHasher.verify(edited))
    }

    @Test
    fun nullAndTextN_doNotCollide() {
        assertNotEquals(IntegrityHasher.hashOf(sample(content = null)), IntegrityHasher.hashOf(sample(content = "N")))
    }

    @Test
    fun userAnnotationsAndVerificationFields_doNotChangeTheHash() {
        val base = sealed(sample())
        val annotated = base.copy(isStarred = true, verificationStatus = VerificationStatus.INTACT, lastVerifiedAt = 5L)
        assertEquals(IntegrityHasher.hashOf(base), IntegrityHasher.hashOf(annotated))
        assertEquals(VerificationStatus.INTACT, IntegrityHasher.verify(annotated))
    }
}
