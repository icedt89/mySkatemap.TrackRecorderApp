package com.janhafner.myskatemap.apps.trackrecorder.services.distance

import com.janhafner.myskatemap.apps.trackrecorder.common.IDestroyable
import com.janhafner.myskatemap.apps.trackrecorder.services.models.Location
import io.reactivex.Observable

internal interface IDistanceCalculator : IDestroyable {
    val distanceCalculated: Observable<Float>

    val distance: Float

    fun clear()

    fun addAll(locations: List<Location>)

    fun add(location: Location)
}