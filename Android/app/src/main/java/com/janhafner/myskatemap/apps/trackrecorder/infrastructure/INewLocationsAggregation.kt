package com.janhafner.myskatemap.apps.trackrecorder.infrastructure

import com.janhafner.myskatemap.apps.trackrecorder.common.IDestroyable
import com.janhafner.myskatemap.apps.trackrecorder.common.aggregations.INewAggregation
import com.janhafner.myskatemap.apps.trackrecorder.common.types.Location

internal interface INewLocationsAggregation
    // DEPRECATED
    : IDestroyable {
    val speed: INewAggregation<Double>

    val altitude: INewAggregation<Double>

    @Deprecated("")
    fun addAll(location: List<Location>)

    @Deprecated("")
    fun add(location: Location)
}