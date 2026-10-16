package com.chatvault.tiktokarchive.capture

import com.chatvault.tiktokarchive.domain.model.CaptureMethod

/**
 * Adapter boundary: every way of getting messages into the archive implements this, so the local archive never
 * depends on how data arrives. Concrete sources arrive in later milestones.
 */
interface CaptureSource {
    val method: CaptureMethod
    suspend fun availability(): SourceAvailability
}

sealed interface SourceAvailability {
    data object Available : SourceAvailability
    data class Unavailable(val reason: String) : SourceAvailability
}
