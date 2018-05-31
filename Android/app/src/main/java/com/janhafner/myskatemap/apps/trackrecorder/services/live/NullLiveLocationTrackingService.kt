package com.janhafner.myskatemap.apps.trackrecorder.services.live

internal final class NullLiveLocationTrackingService : ILiveLocationTrackingService {
    public override fun createSession(): ILiveLocationTrackingSession {
        return NullLiveLocationTrackingSession()
    }
}