package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.live

import com.janhafner.myskatemap.apps.trackrecorder.location.SimpleLocation

internal final class FakeLiveTrackingSession(private val fakeLiveLocationTrackingService: FakeLiveLocationTrackingService, private val sessionId: String) : ILiveTrackingSession {
    public override fun sendLocations(locations: List<SimpleLocation>) {
        this.fakeLiveLocationTrackingService.sendLocations(this.sessionId, locations)
    }

    public override fun endSession() {
        this.fakeLiveLocationTrackingService.endSession(this.sessionId)
    }
}