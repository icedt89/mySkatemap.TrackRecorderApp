package com.janhafner.myskatemap.apps.trackrecorder.location.provider

import com.janhafner.myskatemap.apps.trackrecorder.location.Location
import io.reactivex.Observable

internal interface ILocationProvider {
    fun startLocationUpdates()

    fun stopLocationUpdates()

    fun resetSequenceNumber()

    fun overrideSequenceNumber(sequenceNumber : Int)

    val isActive: Boolean

    val locations: Observable<Location>
}