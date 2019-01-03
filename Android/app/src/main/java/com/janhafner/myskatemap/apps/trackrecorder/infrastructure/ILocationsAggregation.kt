package com.janhafner.myskatemap.apps.trackrecorder.infrastructure

import com.janhafner.myskatemap.apps.trackrecorder.common.IDestroyable
import com.janhafner.myskatemap.apps.trackrecorder.common.aggregations.IAggregation

internal interface ILocationsAggregation : IDestroyable {
    val speed: IAggregation<Double>

    val altitude: IAggregation<Double>
}