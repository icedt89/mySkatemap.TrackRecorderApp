package com.janhafner.myskatemap.apps.trackrecorder.core

import io.reactivex.Observable
import org.joda.time.Period

public interface IObservableTimer : IDestroyable {
    val secondElapsed: Observable<Period>

    val secondsElapsed: Period

    fun start()

    fun reset()

    fun set(elapsedSecondsSinceStart: Int)

    fun stop()
}