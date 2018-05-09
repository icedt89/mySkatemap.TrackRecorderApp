package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.live

import com.janhafner.myskatemap.apps.trackrecorder.location.SimpleLocation

internal interface ILiveTrackingSession {
    fun sendLocations(locations: List<SimpleLocation>)

    fun endSession()
}