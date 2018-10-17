package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider

import com.janhafner.myskatemap.apps.trackrecorder.common.IDestroyable
import com.janhafner.myskatemap.apps.trackrecorder.common.types.Location
import io.reactivex.Observable

internal interface ILocationProvider : IDestroyable {
    fun startLocationUpdates()

    fun stopLocationUpdates()

    val isActive: Boolean

    val activityChanged: Observable<Boolean>

    val locationsReceived: Observable<Location>
}