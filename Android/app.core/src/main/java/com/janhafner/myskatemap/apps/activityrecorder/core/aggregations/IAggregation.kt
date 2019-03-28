package com.janhafner.myskatemap.apps.activityrecorder.core.aggregations

import io.reactivex.Observable

public interface IAggregation<TValue: Number> {
    val minimumValueChanged: Observable<TValue>

    val maximumValueChanged: Observable<TValue>

    val averageValueChanged: Observable<TValue>

    val firstValueChanged: Observable<TValue>

    val latestValueChanged: Observable<TValue>
}