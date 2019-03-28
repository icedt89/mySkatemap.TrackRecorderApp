package com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder

import com.janhafner.myskatemap.apps.activityrecorder.live.LiveLocation
import io.reactivex.Single

internal interface ILiveSessionController {
    fun startSession(): Single<Unit>

    fun sendLocations(locations: List<LiveLocation>): Single<Unit>

    fun endSession(): Single<Unit>
}

