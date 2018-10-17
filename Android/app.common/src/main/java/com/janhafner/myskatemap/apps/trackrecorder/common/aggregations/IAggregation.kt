package com.janhafner.myskatemap.apps.trackrecorder.common.aggregations

import com.janhafner.myskatemap.apps.trackrecorder.common.IDestroyable
import io.reactivex.Observable

public interface IAggregation : IDestroyable {
    val minimumValueChanged: Observable<Float>

    val minimumValue: Float?

    val maximumValueChanged: Observable<Float>

    val maximumValue: Float?

    val averageValueChanged: Observable<Float>

    val averageValue: Float?

    val firstValueChanged: Observable<Float>

    val firstValue: Float?

    val lastValueChanged: Observable<Float>

    val lastValue: Float?

    fun addAll(values: List<Float>)

    fun add(value: Float)
}