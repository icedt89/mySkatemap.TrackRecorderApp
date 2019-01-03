package com.janhafner.myskatemap.apps.trackrecorder.common.aggregations

import com.janhafner.myskatemap.apps.trackrecorder.common.liveAverage
import com.janhafner.myskatemap.apps.trackrecorder.common.liveMax
import com.janhafner.myskatemap.apps.trackrecorder.common.liveMin
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