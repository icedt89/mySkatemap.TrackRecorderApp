package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.statistics

import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

internal final class Statistic {
    private val minimumValueChangedSubject: BehaviorSubject<Float> = BehaviorSubject.create<Float>()
    public val minimumValueChanged: Observable<Float> = this.minimumValueChangedSubject

    public val minimumValue: Float?
        get() = this.minimumValueChangedSubject.value

    private val maximumValueChangedSubject: BehaviorSubject<Float> = BehaviorSubject.create<Float>()
    public val maximumValueChanged: Observable<Float> = this.maximumValueChangedSubject

    public val maximumValue: Float?
        get() = this.maximumValueChangedSubject.value

    private val averageValueChangedSubject: BehaviorSubject<Float> = BehaviorSubject.create<Float>()
    public val averageValueChanged: Observable<Float> = this.averageValueChangedSubject

    public val averageValue: Float?
        get() = this.averageValueChangedSubject.value

    private val firstValueChangedSubject: BehaviorSubject<Float> = BehaviorSubject.create<Float>()
    public val firstValueChanged: Observable<Float> = this.firstValueChangedSubject

    public val firstValue: Float?
        get() = this.firstValueChangedSubject.value

    private val lastValueChangedSubject: BehaviorSubject<Float> = BehaviorSubject.create<Float>()
    public val lastValueChanged: Observable<Float> = this.lastValueChangedSubject

    public val lastValue: Float?
        get() = this.lastValueChangedSubject.value

    private var totalCountOfSamples: Int = 0

    public fun addAll(values: List<Float>) {
        if(!values.any()) {
            return
        }

        val currentMinimumValue = this.minimumValueChangedSubject.value
        val currentMaximumValue = this.maximumValueChangedSubject.value
        val currentAverageValue = this.averageValueChangedSubject.value

        var newMinimumValue = currentMinimumValue
        var newMaximumValue = currentMaximumValue
        var newAverageValue = currentAverageValue

        var lastValue: Float? = null
        for (value in values) {
            this.totalCountOfSamples++

            if(newMinimumValue == null) {
                newMinimumValue = value
            }

            if(newMaximumValue == null) {
                newMaximumValue = value
            }

            if(value > newMaximumValue) {
                newMaximumValue = value
            }

            if(value < newMinimumValue) {
                newMinimumValue = value
            }

            if(this.firstValueChangedSubject.value == null) {
                this.firstValueChangedSubject.onNext(value)
            }

            if(newAverageValue == null) {
                newAverageValue = 0.0f
            }

            newAverageValue = (newAverageValue + value) / this.totalCountOfSamples

            lastValue = value
        }

        if(newMinimumValue < currentMinimumValue) {
            this.minimumValueChangedSubject.onNext(newMinimumValue)
        }

        if(newMaximumValue > currentMaximumValue) {
            this.maximumValueChangedSubject.onNext(newMaximumValue)
        }

        this.averageValueChangedSubject.onNext(newAverageValue)
        this.lastValueChangedSubject.onNext(lastValue!!)
    }

    public fun add(value: Float) {
        this.totalCountOfSamples++

        if(this.minimumValueChangedSubject.value == null) {
            this.minimumValueChangedSubject.onNext(value)
        }

        if(this.maximumValueChangedSubject.value == null) {
            this.maximumValueChangedSubject.onNext(value)
        }

        if(value > this.maximumValueChangedSubject.value) {
            this.maximumValueChangedSubject.onNext(value)
        }

        if(value < this.minimumValueChangedSubject.value) {
            this.minimumValueChangedSubject.onNext(value)
        }

        if(this.firstValueChangedSubject.value == null) {
            this.firstValueChangedSubject.onNext(value)
        }

        this.lastValueChangedSubject.onNext(value)

        var currentAverage = this.averageValueChangedSubject.value
        if(currentAverage == null) {
            currentAverage = 0.0f
        }
        currentAverage = (currentAverage + value) / this.totalCountOfSamples

        this.averageValueChangedSubject.onNext(currentAverage)
    }

    public override fun toString(): String {
        return "Statistic[min:${this.minimumValue}; max:${this.maximumValue}; avg:${this.averageValue}; first:${this.firstValue}; last:${this.lastValue}"
    }
}