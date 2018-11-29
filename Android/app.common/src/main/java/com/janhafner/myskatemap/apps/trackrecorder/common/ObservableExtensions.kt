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

// TODO: Remove if not used
public fun <Upstream> Observable<List<Upstream>>.liveCount() : Observable<Int> {
    var totalCount = 0

    return this.map {
        ++totalCount
    }
}

// TODO: Remove if not used
public fun <Upstream> Observable<Upstream>.withCount() : Observable<Counted<Upstream>> {
    var totalCount = 0

    return this.map {
        Counted(++totalCount, it)
    }
}