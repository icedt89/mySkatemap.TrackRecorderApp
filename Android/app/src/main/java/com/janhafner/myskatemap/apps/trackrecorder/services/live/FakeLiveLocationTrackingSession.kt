package com.janhafner.myskatemap.apps.trackrecorder.services.live

import com.janhafner.myskatemap.apps.trackrecorder.common.SimpleLocation

internal final class FakeLiveLocationTrackingSession(private val fakeLiveLocationTrackingService: FakeLiveLocationTrackingService,
                                                     private val sessionId: String) : ILiveLocationTrackingSession {
    public override fun sendLocations(locations: List<SimpleLocation>) {
        this.fakeLiveLocationTrackingService.sendLocations(this.sessionId, locations)
    }

    public override fun endSession() {
        this.fakeLiveLocationTrackingService.endSession(this.sessionId)
    }
}