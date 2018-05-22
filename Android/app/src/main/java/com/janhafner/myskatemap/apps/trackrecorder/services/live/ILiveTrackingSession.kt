package com.janhafner.myskatemap.apps.trackrecorder.services.live

import com.janhafner.myskatemap.apps.trackrecorder.SimpleLocation

internal interface ILiveTrackingSession {
    fun sendLocations(locations: List<SimpleLocation>)

    fun endSession()
}