package com.janhafner.myskatemap.apps.trackrecorder.common

import com.janhafner.myskatemap.apps.trackrecorder.common.types.Location
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

public fun Observable<Location>.calculateMissingSpeed(): Observable<Location> {
    return this
            .pairWithPrevious()
            .map {
                if(it.first != null && it.second != null && it.second!!.speed == null) {
                    val distanceInMeters = it.first!!.distanceTo(it.second!!)
                    val locationSecondsDifference = Seconds.secondsBetween(it.second!!.capturedAt, it.first!!.capturedAt)

                    it.second!!.speed = Math.abs(locationSecondsDifference.seconds) / distanceInMeters
                }

                if(it.second!!.speed == null) {
                    it.second!!.speed == 0.0f
                }

                it.second
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