package com.janhafner.myskatemap.apps.trackrecorder.common

import io.reactivex.Observable

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

public fun <Upstream> Observable<Float>.liveAverageFloat() : Observable<Double> {
    val values = ArrayList<Float>()

    return this.map {
        values.add(it)

        values.average()
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