package com.chatvault.tiktokarchive.ui.components

import com.chatvault.tiktokarchive.domain.model.AttachmentStatus
import com.chatvault.tiktokarchive.domain.model.CaptureMethod
import com.chatvault.tiktokarchive.domain.model.Direction
import com.chatvault.tiktokarchive.domain.model.MessageType
import com.chatvault.tiktokarchive.domain.model.VerificationStatus

fun CaptureMethod.label(): String = when (this) {
    CaptureMethod.DEMO -> "Demo data (not real evidence)"
    CaptureMethod.OFFICIAL_API -> "Official TikTok API"
    CaptureMethod.DATA_EXPORT_IMPORT -> "Imported TikTok data download"
    CaptureMethod.NOTIFICATION -> "Notification capture"
}

fun VerificationStatus.label(): String = when (this) {
    VerificationStatus.UNVERIFIED -> "Not verified yet"
    VerificationStatus.INTACT -> "Unchanged since capture"
    VerificationStatus.MODIFIED -> "Changed since capture (failed check)"
}

fun Direction.label(): String = when (this) {
    Direction.INCOMING -> "Received"
    Direction.OUTGOING -> "Sent"
    Direction.UNKNOWN -> "Unknown (source did not say)"
}

fun MessageType.label(): String = when (this) {
    MessageType.TEXT -> "Text"
    MessageType.IMAGE -> "Image"
    MessageType.VIDEO -> "Video"
    MessageType.ATTACHMENT -> "Attachment"
    MessageType.UNKNOWN -> "Unknown"
}

fun AttachmentStatus.label(): String = when (this) {
    AttachmentStatus.AVAILABLE -> "File available"
    AttachmentStatus.MISSING -> "File missing"
    AttachmentStatus.NOT_CAPTURED -> "Not captured (only the placeholder exists)"
}
