package com.janhafner.myskatemap.apps.trackrecorder.common.aggregations

import com.janhafner.myskatemap.apps.trackrecorder.common.IDestroyable
import io.reactivex.Observable

public interface IAggregation
    // DEPRECATED
    : IDestroyable {
    val minimumValueChanged: Observable<Float>

    val maximumValueChanged: Observable<Float>

    val averageValueChanged: Observable<Float>

    val firstValueChanged: Observable<Float>

    val lastValueChanged: Observable<Float>

    @Deprecated("")
    fun addAll(values: List<Float>)

    @Deprecated("")
    fun add(value: Float)
}