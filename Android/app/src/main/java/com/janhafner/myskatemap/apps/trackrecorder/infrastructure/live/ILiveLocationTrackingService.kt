package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.live

internal interface ILiveLocationTrackingService {
    public fun createSession(): ILiveTrackingSession
}

