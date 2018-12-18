package com.janhafner.myskatemap.apps.trackrecorder.infrastructure

import com.janhafner.myskatemap.apps.trackrecorder.common.IDestroyable
import com.janhafner.myskatemap.apps.trackrecorder.common.aggregations.IAggregation
import com.janhafner.myskatemap.apps.trackrecorder.common.types.Location

internal interface ILocationsAggregation
    // DEPRECATED
    : IDestroyable {
    val speed: IAggregation

    val altitude: IAggregation

    @Deprecated("")
    fun addAll(location: List<Location>)

    @Deprecated("")
    fun add(location: Location)
}