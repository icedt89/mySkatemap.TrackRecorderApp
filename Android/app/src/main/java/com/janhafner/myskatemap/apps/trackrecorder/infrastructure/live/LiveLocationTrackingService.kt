package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.live

import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.delete
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.post
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.put
import com.janhafner.myskatemap.apps.trackrecorder.location.SimpleLocation
import java.util.*

internal final class LiveLocationTrackingService(private val jsonRestApiClient: JsonRestApiClient)
    : ILiveLocationTrackingService {
    private val baseUrl: String = "https://api.myskatemap.io"

    public override fun createSession(): ILiveTrackingSession {
        // POST /api/live
        // Server side: Store live session only in memory as long as endSession(...) is not called
        val fullUrl = this.jsonRestApiClient.buildFullUrl(this.baseUrl, "/live")

        // TODO: Optain session id from response
        this.jsonRestApiClient.post(fullUrl, null)

        val sessionId = UUID.randomUUID().toString()

        return LiveTrackingSession(this, sessionId)
    }

    public fun sendLocations(sessionId: String, locations: List<SimpleLocation>) {
        // PUT /api/live/{sessiondId}
        val fullUrl = this.jsonRestApiClient.buildFullUrl(this.baseUrl, "/live/${sessionId}")

        // TODO: Optain session id from response
        this.jsonRestApiClient.put(fullUrl, locations)
    }

    public fun endSession(sessionId: String) {
        // DELETE /api/live/{sessionId}
        val fullUrl = this.jsonRestApiClient.buildFullUrl(this.baseUrl, "/live/${sessionId}")

        this.jsonRestApiClient.delete(fullUrl)
    }
}