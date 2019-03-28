package com.janhafner.myskatemap.apps.activityrecorder.core.aggregations

import com.janhafner.myskatemap.apps.activityrecorder.core.liveAverage
import com.janhafner.myskatemap.apps.activityrecorder.core.liveMax
import com.janhafner.myskatemap.apps.activityrecorder.core.liveMin
import io.reactivex.Observable

public final class Aggregation(values: Observable<Double>) : IAggregation<Double> {
    public override val minimumValueChanged = values
            .liveMin()
            .replay(1)
            .autoConnect()

    public override val maximumValueChanged = values
            .liveMax()
            .replay(1)
            .autoConnect()

    public override val averageValueChanged = values
            .liveAverage()
            .replay(1)
            .autoConnect()

    public override val firstValueChanged = values
            .first(0.0)
            .toObservable()
            .replay(1)
            .autoConnect()

    public override val latestValueChanged = values
            .startWith(0.0)
            .replay(1)
            .autoConnect()
}