package com.janhafner.myskatemap.apps.trackrecorder.common

import io.reactivex.Observable

public interface IObservableTimeout : IDestroyable {
    val timedOut: Observable<Unit>

    fun restart()

    fun stop()
}