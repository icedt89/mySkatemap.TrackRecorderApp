package com.janhafner.myskatemap.apps.trackrecorder

import io.reactivex.Observable
import org.joda.time.Period

internal interface IObservableTimer : IDestroyable {
    val secondElapsed: Observable<Period>

    val secondsElapsed: Period

    val isRunningChanged: Observable<Boolean>

    val isRunning: Boolean

    val timerReset: Observable<Long>

    fun start()

    fun reset()

    fun set(elapsedSecondsSinceStart: Int)

    fun stop()
}