package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.live

import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.SimpleLocation

internal interface ILiveTrackingSession {
    fun sendLocations(locations: List<SimpleLocation>)

    fun endSession()
}