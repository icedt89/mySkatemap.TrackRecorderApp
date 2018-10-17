package com.janhafner.myskatemap.apps.trackrecorder.common

import com.janhafner.myskatemap.apps.trackrecorder.common.types.Location
import io.reactivex.Observable
import org.joda.time.Seconds

public fun <Upstream> Observable<Upstream>.pairWithPrevious() : Observable<Pair<Upstream?, Upstream?>> {
    return this.scan(Pair<Upstream?, Upstream?>(null, null), { t1, t2 ->
        Pair(t1.second, t2!!)
    }).filter {
        // Filter seed [Pair(null, null)]
        it.second != null
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
                    if (distanceInMeters != 0.0f) {
                        val locationSecondsDifference = Seconds.secondsBetween(it.second!!.capturedAt, it.first!!.capturedAt)

                        it.second!!.speed = Math.abs(locationSecondsDifference.seconds) / distanceInMeters
                    } else {
                        it.second!!.speed = 0.0f
                    }
                }

                it.second
            }
}

public fun Observable<Location>.dropLocationsNotInDistance(maximumDistance: Double, includeEdge: Boolean = true): Observable<Location> {
    return this.pairWithPrevious()
            .filter {
                if (it.first == null) {
                    // Dont filter first real value [Pair(null, location)]
                    true
                } else {
                    // Apply distance filter to suppress emitting values to close
                    !it.second!!.isInDistance(it.first!!, maximumDistance, includeEdge)
                }
            }
            .map {
                it.second
            }
}

public fun <Upstream> Observable<List<Upstream>>.liveCount() : Observable<Int> {
    var totalCount = 0

    return this.map {
        ++totalCount
    }
}

public fun <Upstream> Observable<Upstream>.withCount() : Observable<Counted<Upstream>> {
    var totalCount = 0

    return this.map {
        Counted(++totalCount, it)
    }
}