package com.janhafner.myskatemap.apps.trackrecorder.infrastructure

import com.janhafner.myskatemap.apps.trackrecorder.common.aggregations.Aggregation
import com.janhafner.myskatemap.apps.trackrecorder.common.IDestroyable
import com.janhafner.myskatemap.apps.trackrecorder.common.types.Location

internal interface ILocationsAggregation : IDestroyable {
    val speed: Aggregation

    val altitude: Aggregation

    fun addAll(location: List<Location>)

    fun add(location: Location)
}