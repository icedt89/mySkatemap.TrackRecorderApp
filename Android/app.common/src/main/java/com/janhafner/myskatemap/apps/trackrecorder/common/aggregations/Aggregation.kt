package com.janhafner.myskatemap.apps.trackrecorder.common.aggregations

import com.janhafner.myskatemap.apps.trackrecorder.common.ObjectDestroyedException
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject

@Deprecated("")
public final class Aggregation : IAggregation {
    private val minimumValueChangedSubject: BehaviorSubject<Float> = BehaviorSubject.create<Float>()
    public override val minimumValueChanged: Observable<Float> = this.minimumValueChangedSubject.subscribeOn(Schedulers.computation())

    private val maximumValueChangedSubject: BehaviorSubject<Float> = BehaviorSubject.create<Float>()
    public override val maximumValueChanged: Observable<Float> = this.maximumValueChangedSubject.subscribeOn(Schedulers.computation())

    private val averageComputationValueSource: ArrayList<Float> = ArrayList()
    private val averageValueChangedSubject: BehaviorSubject<Float> = BehaviorSubject.create<Float>()
    public override val averageValueChanged: Observable<Float> = this.averageValueChangedSubject.subscribeOn(Schedulers.computation())

    private val firstValueChangedSubject: BehaviorSubject<Float> = BehaviorSubject.create<Float>()
    public override val firstValueChanged: Observable<Float> = this.firstValueChangedSubject.subscribeOn(Schedulers.computation())

    private val lastValueChangedSubject: BehaviorSubject<Float> = BehaviorSubject.create<Float>()
    public override val lastValueChanged: Observable<Float> = this.lastValueChangedSubject.subscribeOn(Schedulers.computation())

    public override fun addAll(values: List<Float>) {
        if(this.isDestroyed) {
            throw ObjectDestroyedException()
        }

        if(!values.any()) {
            return
        }

        val currentMinimumValue = this.minimumValueChangedSubject.value
        val currentMaximumValue = this.maximumValueChangedSubject.value

        var newMinimumValue = currentMinimumValue
        var newMaximumValue = currentMaximumValue

        var lastValue: Float? = null
        for (value in values) {
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

            this.averageComputationValueSource.add(value)

            lastValue = value
        }

        if((currentMinimumValue == null && newMinimumValue != null) || (newMinimumValue!! < currentMinimumValue!!)) {
            this.minimumValueChangedSubject.onNext(newMinimumValue)
        }

        if((currentMaximumValue == null && newMaximumValue != null) || (newMaximumValue!! > currentMaximumValue!!)) {
            this.maximumValueChangedSubject.onNext(newMaximumValue)
        }


        val averageValue = this.averageComputationValueSource.average().toFloat()

        this.averageValueChangedSubject.onNext(averageValue)
        this.lastValueChangedSubject.onNext(lastValue!!)
    }

    public override fun add(value: Float) {
        if(this.isDestroyed) {
            throw ObjectDestroyedException()
        }

        if(this.minimumValueChangedSubject.value == null) {
            this.minimumValueChangedSubject.onNext(value)
        }

        if(this.maximumValueChangedSubject.value == null) {
            this.maximumValueChangedSubject.onNext(value)
        }

        if(value > this.maximumValueChangedSubject.value!!) {
            this.maximumValueChangedSubject.onNext(value)
        }

        if(value < this.minimumValueChangedSubject.value!!) {
            this.minimumValueChangedSubject.onNext(value)
        }

        if(this.firstValueChangedSubject.value == null) {
            this.firstValueChangedSubject.onNext(value)
        }

        this.lastValueChangedSubject.onNext(value)

        this.averageComputationValueSource.add(value)
        val currentAverage = this.averageComputationValueSource.average().toFloat()

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
}