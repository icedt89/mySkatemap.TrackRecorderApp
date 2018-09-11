package com.janhafner.myskatemap.apps.trackrecorder.common

import io.reactivex.Observable
import org.joda.time.Period

public interface IObservableTimer : IDestroyable {
    val secondElapsed: Observable<Period>

    val secondsElapsed: Period

    val isRunningChanged: Observable<Boolean>

    val isRunning: Boolean

    val timerReset: Observable<Unit>

    fun start()

    fun reset()

    fun set(elapsedSecondsSinceStart: Int)

    fun stop()
}