package com.janhafner.myskatemap.apps.trackrecorder.common

import android.util.Log
import android.util.TimingLogger
import com.janhafner.myskatemap.apps.trackrecorder.common.types.Location
import com.janhafner.myskatemap.apps.trackrecorder.common.types.Sex
import io.reactivex.Observable
import org.joda.time.Seconds



public fun <Upstream> Observable<Upstream>.pairWithPrevious() : Observable<Pair<Upstream?, Upstream?>> {
    return this.scan(Pair<Upstream?, Upstream?>(null, null)) { t1, t2 ->
        Pair(t1.second, t2!!)
    }.filter {
        // Filter seed [Pair(null, null)]
        it.second != null && it.first != null
    }
}

public fun <Upstream> Observable<List<Upstream>>.filterNotEmpty() : Observable<List<Upstream>> {
    return this
            .filter {
                it.any()
            }
}

public fun Observable<Int>.burnedEnergy(weightInKilograms: Float,
                                        heightInCentimeters: Int,
                                        ageInYears: Int,
                                        sex: Sex,
                                        metValue: Float): Observable<Float> {
    val basalMetabolicFactorSet: BasalMetabolicFactorSet
    if (sex == Sex.Male) {
        basalMetabolicFactorSet = BasalMetabolicFactorSet.male
    } else {
        basalMetabolicFactorSet = BasalMetabolicFactorSet.female
    }

    // https://www.blitzresults.com/en/calories-burned/
    // https://en.wikipedia.org/wiki/Harris%E2%80%93Benedict_equation
    var basalMetabolicRate = (basalMetabolicFactorSet.factor1 * weightInKilograms)
    + (basalMetabolicFactorSet.factor2 * heightInCentimeters)
    - (basalMetabolicFactorSet.factor3 * ageInYears)
    if (sex == Sex.Male) {
        basalMetabolicRate = basalMetabolicRate + basalMetabolicFactorSet.factor4
    } else {
        basalMetabolicRate = basalMetabolicRate - basalMetabolicFactorSet.factor4
    }

    val partialCompleteFormula = basalMetabolicRate / 24.0f * metValue

    return this.map {
        val timeInHours = ((it / 60.0f) / 60.0f)

        partialCompleteFormula * timeInHours
    }
}

public fun Observable<Location>.calculateMissingSpeed(): Observable<Location> {
    return this
            .pairWithPrevious()
            .map {
                if(it.first != null && it.second != null && it.second!!.speed == null) {
                    val distanceInMeters = it.first!!.toLiteAndroidLocation().distanceTo(it.second!!.toLiteAndroidLocation())
                    val locationSecondsDifference = Seconds.secondsBetween(it.second!!.time, it.first!!.time)

                    it.second!!.speed = Math.abs(locationSecondsDifference.seconds) / distanceInMeters
                }

                if(it.second!!.speed == null) {
                    it.second!!.speed == 0.0f
                }

                it.second
            }
}

public fun Observable<android.location.Location>.inDistance(maximumDistanceInMeter: Float, includeEdge: Boolean = true): Observable<android.location.Location> {
    return this.filter {
        it.isInDistance(it, maximumDistanceInMeter, includeEdge)
    }
}

public fun <Upstream> Observable<Upstream>.withCount() : Observable<Counted<Upstream>> {
    var currentCount = 0

    return this.map {
        Counted(currentCount++, it)
    }
}

public fun Observable<Double>.liveMin(): Observable<Double> {
    var currentMin = Double.MAX_VALUE

    return this
            .filter {
        if(it < currentMin) {
            currentMin = it

            true
        } else {
            false
        }
    }
}

public fun Observable<Double>.liveMax(): Observable<Double> {
    var currentMax = Double.MIN_VALUE

    return this
        .filter {
        if(it > currentMax) {
            currentMax = it

            true
        } else {
            false
        }
    }
}

public fun Observable<Double>.liveAverage(): Observable<Double> {
    val valuesForAverage = mutableListOf<Double>()

    return this
            .doOnNext {
                valuesForAverage.add(it)
            }
            .map {
                valuesForAverage.average()
            }
}

public fun Observable<List<android.location.Location>>.distance(): Observable<Float> {
    var distance: Float = 0.0f
    var lastAccessedLocation: android.location.Location? = null

    return this.map {
        if(!it.any()) {
            distance
        } else {
            var result = distance

            if(lastAccessedLocation != null) {
                val timings = TimingLogger("distance_measure", "A")

                val distance1 = lastAccessedLocation!!.distanceTo(it.first())

                timings.addSplit("B")

                val distance2 = haversine(lastAccessedLocation!!.latitude, it.first().latitude, lastAccessedLocation!!.longitude, it.first().longitude)

                timings.dumpToLog()

                // Compute distance between last point of last path and first point of the new path
                result += lastAccessedLocation!!.distanceTo(it.first())

                Log.i("distance1", distance1.toString())
                Log.i("distance2", distance2.toString())
            }

            // Calculate distance of new path
            result += it.calculateDistance2()

            lastAccessedLocation = it.last()

            distance = result

            result
        }
    }
}