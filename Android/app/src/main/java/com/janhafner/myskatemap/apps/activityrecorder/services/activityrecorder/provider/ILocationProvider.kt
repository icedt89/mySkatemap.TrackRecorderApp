package com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.provider

import com.janhafner.myskatemap.apps.activityrecorder.core.IDestroyable
import com.janhafner.myskatemap.apps.activityrecorder.core.types.Location
import io.reactivex.Observable

internal interface ILocationProvider : IDestroyable {
    fun startLocationUpdates()

    fun stopLocationUpdates()

    val isActive: Boolean

    val locationsReceived: Observable<Location>
}