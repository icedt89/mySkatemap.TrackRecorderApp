package com.janhafner.myskatemap.apps.trackrecorder.services.live

import com.janhafner.myskatemap.apps.trackrecorder.common.SimpleLocation

internal interface ILiveLocationTrackingSession {
    fun sendLocations(locations: List<SimpleLocation>)

    fun endSession()
}