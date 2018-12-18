package com.janhafner.myskatemap.apps.trackrecorder.common.aggregations

import com.janhafner.myskatemap.apps.trackrecorder.common.IDestroyable
import io.reactivex.Observable

public interface INewAggregation<TValue: Number>
// DEPRECATED
    : IDestroyable {
    val minimumValueChanged: Observable<TValue>

    val maximumValueChanged: Observable<TValue>

    val averageValueChanged: Observable<TValue>

    val firstValueChanged: Observable<TValue>

    val lastValueChanged: Observable<TValue>

    @Deprecated("")
    fun addAll(values: List<TValue>)

    @Deprecated("")
    fun add(value: TValue)
}