package com.janhafner.myskatemap.apps.trackrecorder.core.adapter

import io.reactivex.Observable
import io.reactivex.disposables.Disposable

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