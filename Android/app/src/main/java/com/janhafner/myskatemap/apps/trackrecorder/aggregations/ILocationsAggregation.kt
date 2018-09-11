package com.janhafner.myskatemap.apps.trackrecorder.aggregations

import com.janhafner.myskatemap.apps.trackrecorder.common.IDestroyable
import com.janhafner.myskatemap.apps.trackrecorder.services.models.Location

internal interface ILocationsAggregation : IDestroyable {
    val speed: Aggregation

    val altitude: Aggregation

    fun addAll(location: List<Location>)

    fun add(location: Location)
}