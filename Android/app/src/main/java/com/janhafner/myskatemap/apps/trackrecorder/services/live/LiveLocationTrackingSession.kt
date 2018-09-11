package com.janhafner.myskatemap.apps.trackrecorder.services.live

import com.janhafner.myskatemap.apps.trackrecorder.common.SimpleLocation

internal final class LiveLocationTrackingSession(private val liveLocationTrackingService: LiveLocationTrackingService, private val sessionId: String) : ILiveLocationTrackingSession {
    public override fun sendLocations(locations: List<SimpleLocation>) {
        this.liveLocationTrackingService.sendLocations(this.sessionId, locations)
    }

    public override fun endSession() {
        this.liveLocationTrackingService.endSession(this.sessionId)
    }
}