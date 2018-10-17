package com.janhafner.myskatemap.apps.trackrecorder.distancecalculation

import com.janhafner.myskatemap.apps.trackrecorder.common.distanceTo
import com.janhafner.myskatemap.apps.trackrecorder.common.types.Location
import io.reactivex.Observable
import io.reactivex.ObservableSource

public final class DistanceCalculatorTransformer(private val distanceCalculator: IDistanceCalculator)
    : io.reactivex.ObservableTransformer<List<Location>, Float> {
    private var lastAccessedLocation: Location? = null

    private var distance: Float = 0.0f

    private fun calculateDistance(locations: List<Location>): Float {
        if(!locations.any()) {
            return this.distance
        }

        var result = this.distance

        if(this.lastAccessedLocation != null) {
            // Compute distance between last point of last path and first point of the new path
            result += this.lastAccessedLocation!!.distanceTo(locations.first())
        }

        // Calculate distance of new path
        result += this.distanceCalculator.calculateDistance(locations)

        this.lastAccessedLocation = locations.last()

        this.distance = result

        return result
    }

    public override fun apply(upstream: Observable<List<Location>>) : ObservableSource<Float> {
        return upstream
                .map {
                    this.calculateDistance(it)
                }
    }
}