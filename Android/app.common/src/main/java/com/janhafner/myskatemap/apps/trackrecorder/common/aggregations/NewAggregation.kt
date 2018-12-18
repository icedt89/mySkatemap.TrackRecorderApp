package com.janhafner.myskatemap.apps.trackrecorder.common.aggregations

import com.janhafner.myskatemap.apps.trackrecorder.common.pairWithPrevious
import io.reactivex.Observable

public final class NewAggregation(values: Observable<Double>) : INewAggregation<Double> {
    override val minimumValueChanged = values
            .pairWithPrevious()
            .filter {
                it.first == null || it.second!! < it.first!!
            }
            .map {
                it.second!!
            }
            .replay(1)
            .autoConnect()

    override val maximumValueChanged = values
            .pairWithPrevious()
            .filter {
                it.first == null || it.second!! > it.first!!
            }
            .map {
                it.second!!
            }
            .replay(1)
            .autoConnect()

    override val averageValueChanged = values
            .pairWithPrevious()
            .filter {
                it.first != null && it.second != null
            }
            .map {
                (it.first !! + it.second!!) / 2
            }
            .replay(1)
            .autoConnect()

    override val firstValueChanged = values
            .first(0.0)
            .toObservable()
            .replay(1)
            .autoConnect()

    override val lastValueChanged = values
            .replay(1)
            .autoConnect()

    public override fun addAll(values: List<Double>) {
    }

    public override fun add(value: Double) {
    }

    public override fun destroy() {
    }
}