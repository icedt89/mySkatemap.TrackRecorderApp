package com.janhafner.myskatemap.apps.trackrecorder.infrastructure

import com.janhafner.myskatemap.apps.trackrecorder.core.IDestroyable
import com.janhafner.myskatemap.apps.trackrecorder.core.aggregations.IAggregation

internal interface ILocationsAggregation : IDestroyable {
    val speed: IAggregation<Double>

    val altitude: IAggregation<Double>
}