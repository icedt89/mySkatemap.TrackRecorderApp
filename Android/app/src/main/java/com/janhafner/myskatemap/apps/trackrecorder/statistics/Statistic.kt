package com.janhafner.myskatemap.apps.trackrecorder.statistics

import android.util.Log
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

internal final class Statistic : IStatistic {
    private val minimumValueChangedSubject: BehaviorSubject<Float> = BehaviorSubject.create<Float>()
    public override val minimumValueChanged: Observable<Float> = this.minimumValueChangedSubject

    public override val minimumValue: Float?
        get() = this.minimumValueChangedSubject.value

    private val maximumValueChangedSubject: BehaviorSubject<Float> = BehaviorSubject.create<Float>()
    public override val maximumValueChanged: Observable<Float> = this.maximumValueChangedSubject

    public override val maximumValue: Float?
        get() = this.maximumValueChangedSubject.value

    private val averageValueChangedSubject: BehaviorSubject<Float> = BehaviorSubject.create<Float>()
    public override val averageValueChanged: Observable<Float> = this.averageValueChangedSubject

    public override val averageValue: Float?
        get() = this.averageValueChangedSubject.value

    private val firstValueChangedSubject: BehaviorSubject<Float> = BehaviorSubject.create<Float>()
    public override val firstValueChanged: Observable<Float> = this.firstValueChangedSubject

    public override val firstValue: Float?
        get() = this.firstValueChangedSubject.value

    private val lastValueChangedSubject: BehaviorSubject<Float> = BehaviorSubject.create<Float>()
    public override val lastValueChanged: Observable<Float> = this.lastValueChangedSubject

    public override val lastValue: Float?
        get() = this.lastValueChangedSubject.value

    private var totalCountOfSamples: Int = 0

    public override fun addAll(values: List<Float>) {
        if(this.isDestroyed) {
            throw IllegalStateException("Object is destroyed!")
        }

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

        if((currentMinimumValue == null && newMinimumValue != null) || (newMinimumValue < currentMinimumValue)) {
            this.minimumValueChangedSubject.onNext(newMinimumValue)
        }

        if((currentMaximumValue == null && newMaximumValue != null) || (newMaximumValue > currentMaximumValue)) {
            this.maximumValueChangedSubject.onNext(newMaximumValue)
        }

        this.averageValueChangedSubject.onNext(newAverageValue)
        this.lastValueChangedSubject.onNext(lastValue!!)
    }

    public override fun add(value: Float) {
        if(this.isDestroyed) {
            throw IllegalStateException("Object is destroyed!")
        }

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

    private var isDestroyed: Boolean = false
    public override fun destroy() {
        if(this.isDestroyed) {
            return
        }

        this.averageValueChangedSubject.onComplete()
        this.firstValueChangedSubject.onComplete()
        this.lastValueChangedSubject.onComplete()
        this.maximumValueChangedSubject.onComplete()
        this.minimumValueChangedSubject.onComplete()

        this.isDestroyed = true
    }

    public override fun toString(): String {
        return "Statistic[min:${this.minimumValue};max:${this.maximumValue};avg:${this.averageValue};first:${this.firstValue};last:${this.lastValue}"
    }
}