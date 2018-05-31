package com.janhafner.myskatemap.apps.trackrecorder.services.live

import com.janhafner.myskatemap.apps.trackrecorder.SimpleLocation

internal final class NullLiveLocationTrackingSession : ILiveLocationTrackingSession {
    public override fun sendLocations(locations: List<SimpleLocation>) {
    }

    public override fun endSession() {
    }
}