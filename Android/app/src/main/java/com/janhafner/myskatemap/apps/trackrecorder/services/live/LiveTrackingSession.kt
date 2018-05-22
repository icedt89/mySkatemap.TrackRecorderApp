package com.janhafner.myskatemap.apps.trackrecorder.services.live

import com.janhafner.myskatemap.apps.trackrecorder.SimpleLocation

internal final class LiveTrackingSession(private val liveLocationTrackingService: LiveLocationTrackingService, private val sessionId: String) : ILiveTrackingSession {
    public override fun sendLocations(locations: List<SimpleLocation>) {
        this.liveLocationTrackingService.sendLocations(this.sessionId, locations)
    }

    public override fun endSession() {
        this.liveLocationTrackingService.endSession(this.sessionId)
    }
}