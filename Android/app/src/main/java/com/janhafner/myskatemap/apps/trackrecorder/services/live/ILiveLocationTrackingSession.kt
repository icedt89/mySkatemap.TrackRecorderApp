package com.janhafner.myskatemap.apps.trackrecorder.services.live

import com.janhafner.myskatemap.apps.trackrecorder.SimpleLocation

internal interface ILiveLocationTrackingSession {
    fun sendLocations(locations: List<SimpleLocation>)

    fun endSession()
}