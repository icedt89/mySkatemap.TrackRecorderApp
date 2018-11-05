package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder

import com.janhafner.myskatemap.apps.trackrecorder.live.LiveLocation

internal interface ILiveSessionController {
    fun startSession()

    fun sendLocations(locations: List<LiveLocation>)

    fun endSession()
}

