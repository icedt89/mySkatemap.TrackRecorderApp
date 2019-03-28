package com.janhafner.myskatemap.apps.activityrecorder.infrastructure

import com.janhafner.myskatemap.apps.activityrecorder.core.IDestroyable
import com.janhafner.myskatemap.apps.activityrecorder.core.aggregations.IAggregation

internal interface ILocationsAggregation : IDestroyable {
    val speed: IAggregation<Double>

    val altitude: IAggregation<Double>
}