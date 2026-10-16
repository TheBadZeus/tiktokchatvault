package com.chatvault.tiktokarchive.capture.officialapi

import com.chatvault.tiktokarchive.capture.CaptureSource
import com.chatvault.tiktokarchive.capture.SourceAvailability
import com.chatvault.tiktokarchive.domain.model.CaptureMethod

/**
 * Placeholder. No TikTok endpoints, scopes or auth flows are assumed here. Milestone 4 starts by reading TikTok's
 * current developer documentation to find out whether ANY authorized API can read a user's own direct messages.
 * Until that is confirmed, this source reports itself as unavailable.
 */
class OfficialApiSource : CaptureSource {
    override val method = CaptureMethod.OFFICIAL_API
    override suspend fun availability(): SourceAvailability =
        SourceAvailability.Unavailable("Not confirmed: no official API for reading direct messages has been verified yet.")
}
