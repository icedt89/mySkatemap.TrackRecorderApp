package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider

import com.janhafner.myskatemap.apps.trackrecorder.core.IDestroyable
import com.janhafner.myskatemap.apps.trackrecorder.core.types.Location
import io.reactivex.Observable

internal interface ILocationProvider : IDestroyable {
    fun startLocationUpdates()

    fun stopLocationUpdates()

    val isActive: Boolean

    val locationsReceived: Observable<Location>
}