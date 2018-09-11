package com.janhafner.myskatemap.apps.trackrecorder.common

import io.reactivex.Observable
import io.reactivex.disposables.Disposable

public fun <TSource> Observable<TSource>.pairWithPrevious() : Observable<Pair<TSource?, TSource?>> {
    return this.scan(Pair<TSource?, TSource?>(null, null), {
        t1, t2 ->
        Pair(t1.second, t2!!)
    })
            .filter {
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

public fun <T> ArrayRecyclerViewAdapter<T>.subscribeTo(items: Observable<T>, clearOnComplete: Boolean = false): Disposable {
    var result = items

    if(clearOnComplete) {
        result = result.doOnComplete{
            this.clear()
        }
    }

    return result
            .subscribe {
                this.add(it)
            }
}

public fun <T> ArrayRecyclerViewAdapter<T>.subscribeToList(items: Observable<List<T>>, clearOnComplete: Boolean = false): Disposable {
    var result = items

    if(clearOnComplete) {
        result = result.doOnComplete{
            this.clear()
        }
    }

    return result
            .subscribe {
                this.addAll(it)
            }
}

public fun <T> DynamicArrayAdapter<T>.subscribeTo(items: Observable<T>, clearOnComplete: Boolean = false): Disposable {
    var result = items

    if(clearOnComplete) {
        result = result.doOnComplete{
            this.clear()
        }
    }

    return result
            .subscribe {
                this.add(it)
            }
}