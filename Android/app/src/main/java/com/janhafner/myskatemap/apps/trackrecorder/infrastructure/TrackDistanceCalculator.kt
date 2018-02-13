package com.janhafner.myskatemap.apps.trackrecorder.infrastructure

import com.janhafner.myskatemap.apps.trackrecorder.distanceTo
import com.janhafner.myskatemap.apps.trackrecorder.location.Location
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

internal final class TrackDistanceCalculator {
    private var lastAccessLocationForComputation: Location? = null

    private val distanceSubject: BehaviorSubject<Float> = BehaviorSubject.createDefault<Float>(0f)
    public val distanceCalculated: Observable<Float> = this.distanceSubject

    public val distance: Float
        get() = this.distanceSubject.value

    public fun clear() {
        this.lastAccessLocationForComputation = null
        this.distanceSubject.onNext(0f)
    }

    public fun addAll(locations: Iterable<Location>) {
        var newDistance: Float = this.distance
        for(location in locations) {
            val actualDistance = this.computeDistance(newDistance, location)
            if(actualDistance != null) {
                newDistance = actualDistance
            }
        }

        this.distanceSubject.onNext(newDistance)
    }

    private fun computeDistance(seed: Float, location: Location): Float? {
        var result: Float? = null

        if(this.lastAccessLocationForComputation != null) {
            result = seed + this.lastAccessLocationForComputation!!.distanceTo(location)
        }

        this.lastAccessLocationForComputation = location

        return result
    }

    public fun add(location: Location) {
        val newDistance = this.computeDistance(this.distance, location)

        if(newDistance != null){
            this.distanceSubject.onNext(newDistance)
        }
    }
}