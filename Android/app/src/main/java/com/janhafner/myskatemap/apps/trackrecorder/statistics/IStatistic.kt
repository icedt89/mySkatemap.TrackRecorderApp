package com.janhafner.myskatemap.apps.trackrecorder.statistics

import com.janhafner.myskatemap.apps.trackrecorder.IDestroyable
import io.reactivex.Observable

internal interface IStatistic : IDestroyable {
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