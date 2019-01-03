package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder

import com.janhafner.myskatemap.apps.trackrecorder.live.LiveLocation
import io.reactivex.Single

internal interface ILiveSessionController {
    fun startSession(): Single<Unit>

    fun sendLocations(locations: List<LiveLocation>): Single<Unit>

    fun endSession(): Single<Unit>
}

