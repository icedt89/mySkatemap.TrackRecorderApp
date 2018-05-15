package com.janhafner.myskatemap.apps.trackrecorder.infrastructure

import android.os.SystemClock
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import org.joda.time.MutablePeriod
import org.joda.time.Period
import org.joda.time.PeriodType
import java.util.*

internal final class ObservableTimer {
    private val timer: Timer = Timer()

    private var timerTask: TimerTask? = this.createTimerTask()

    public var isRunning: Boolean = false
        private set

    private val secondElapsedSubject: BehaviorSubject<Period> = BehaviorSubject.createDefault<Period>(Period.ZERO)
    public val secondElapsed: Observable<Period> = this.secondElapsedSubject

    private val isRunningSubject: BehaviorSubject<Boolean> = BehaviorSubject.createDefault<Boolean>(false)
    public val isRunningChanged: Observable<Boolean> = this.isRunningSubject

    private val timerResetSubject: Subject<Long> = PublishSubject.create<Long>()
    public val timerReset: Observable<Long> = this.timerResetSubject

    private val elapsedSeconds: MutablePeriod = MutablePeriod(PeriodType.time())

    private fun createTimerTask(): TimerTask {
        return object: TimerTask() {
            override fun run() {
                val self = this@ObservableTimer

                self.elapsedSeconds.addSeconds(1)

                self.secondElapsedSubject.onNext(self.elapsedSeconds.toPeriod())
            }
        }
    }

    public fun reset(elapsedSecondsSinceStart: Int) {
        if (elapsedSecondsSinceStart < 0) {
            throw IllegalArgumentException("elapsedSecondsSinceStart")
        }

        this.elapsedSeconds.clear()
        this.elapsedSeconds.addSeconds(elapsedSecondsSinceStart)

        if (!this.isRunning) {
            this.secondElapsedSubject.onNext(this.elapsedSeconds.toPeriod())
        }

        this.timerResetSubject.onNext(SystemClock.elapsedRealtime())
    }

    public fun reset() {
        this.reset(0)
    }

    public fun start() {
        if (this.isRunning) {
            throw IllegalStateException("Timer is already started!")
        }

        if (this.timerTask == null) {
            this.timerTask = this.createTimerTask()
        }

        this.changeState(true)

        this.timer.scheduleAtFixedRate(this.timerTask, 0, 1000)
    }

    public fun stop() {
        if (!this.isRunning) {
            throw IllegalStateException("Timer is already stopped!")
        }

        this.timerTask!!.cancel()
        this.timerTask = null

        this.changeState(false)
    }

    private fun changeState(isRunning: Boolean) {
        this.isRunning = isRunning

        this.isRunningSubject.onNext(isRunning)
    }
}