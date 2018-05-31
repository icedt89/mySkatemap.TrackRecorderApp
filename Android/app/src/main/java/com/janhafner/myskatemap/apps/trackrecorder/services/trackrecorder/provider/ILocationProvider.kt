package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider

import com.janhafner.myskatemap.apps.trackrecorder.IDestroyable
import com.janhafner.myskatemap.apps.trackrecorder.io.data.Location
import io.reactivex.Observable

internal interface ILocationProvider : IDestroyable {
    fun startLocationUpdates()

    fun stopLocationUpdates()

    fun resetSequenceNumber()

    fun getCurrentLocation(): Location?

    fun overrideSequenceNumber(sequenceNumber: Int)

    val isActive: Boolean

    val activityChanged: Observable<Boolean>

    val sequenceNumberOverridden: Observable<Int>

    val locationsReceived: Observable<Location>
}