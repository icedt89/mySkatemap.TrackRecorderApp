package com.janhafner.myskatemap.apps.trackrecorder.services.distance

import com.janhafner.myskatemap.apps.trackrecorder.distanceTo
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.data.Location
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject

internal final class DistanceCalculator : IDistanceCalculator {
    private var lastAccessLocationForComputation: Location? = null

    private val distanceCalculatedSubject: BehaviorSubject<Float> = BehaviorSubject.createDefault<Float>(0f)
    public override val distanceCalculated: Observable<Float> = this.distanceCalculatedSubject.subscribeOn(Schedulers.computation())

    public override val distance: Float
        get() = this.distanceCalculatedSubject.value

    public override fun clear() {
        if(this.isDestroyed) {
            throw IllegalStateException("Object is destroyed!")
        }

        this.lastAccessLocationForComputation = null
        this.distanceCalculatedSubject.onNext(0f)
    }

    public override fun addAll(locations: List<Location>) {
        if(this.isDestroyed) {
            throw IllegalStateException("Object is destroyed!")
        }

        var newDistance: Float = this.distance
        for(location in locations) {
            val actualDistance = this.computeDistance(newDistance, location)
            if(actualDistance != null) {
                newDistance = actualDistance
            }
        }

        this.distanceCalculatedSubject.onNext(newDistance)
    }

    private fun computeDistance(seed: Float, location: Location): Float? {
        var result: Float? = null

        if(this.lastAccessLocationForComputation != null) {
            result = seed + this.lastAccessLocationForComputation!!.distanceTo(location)
        }

        this.lastAccessLocationForComputation = location

        return result
    }

    public override fun add(location: Location) {
        if(this.isDestroyed) {
            throw IllegalStateException("Object is destroyed!")
        }

        val newDistance = this.computeDistance(this.distance, location)

        if(newDistance != null){
            this.distanceCalculatedSubject.onNext(newDistance)
        }
    }

    private var isDestroyed: Boolean = false
    public override fun destroy() {
        if(this.isDestroyed) {
            return
        }

        this.distanceCalculatedSubject.onComplete()

        this.isDestroyed = true
    }
}