package com.janhafner.myskatemap.apps.trackrecorder.infrastructure

import android.util.Log
import com.janhafner.myskatemap.apps.trackrecorder.distanceTo
import com.janhafner.myskatemap.apps.trackrecorder.location.Location
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

internal final class TrackDistanceCalculator() {
    private val innerList : MutableList<Location> = ArrayList<Location>()

    private var lastAccessLocationForComputation : Location? = null

    private val distanceSubject : BehaviorSubject<Float> = BehaviorSubject.createDefault<Float>(0f)
    public val distanceCalculated : Observable<Float> = this.distanceSubject

    public val distance : Float
        get() = this.distanceSubject.value

    public constructor(locations : Iterable<Location>)
        : this() {
        this.addAll(locations)
    }

    public constructor(location : Location)
        : this() {
        this.add(location)
    }

    private fun computeDistance(location : Location) {
        if(this.lastAccessLocationForComputation != null) {
            val distanceBetween = this.lastAccessLocationForComputation!!.distanceTo(location)

            Log.v("TrackLengthCalculator", "Adding new distance of ${distanceBetween}m")

            val newDistance = this.distance + distanceBetween

            Log.v("TrackLengthCalculator", "Full distance of ${this.innerList.count() + 1} locations is ${newDistance}m")

            this.distanceSubject.onNext(newDistance)
        }

        this.lastAccessLocationForComputation = location
    }

    public fun clear() {
        this.innerList.clear()

        Log.v("TrackLengthCalculator", "All locations cleared, distance reset to 0m ")

        this.distanceSubject.onNext(0f)
    }

    public fun addAll(locations : Iterable<Location>) {
        for(location in locations) {
            this.computeDistance(location)
        }

        this.innerList.addAll(locations)
    }

    public fun add(location : Location) {
        this.computeDistance(location)

        this.innerList.add(location)
    }
}