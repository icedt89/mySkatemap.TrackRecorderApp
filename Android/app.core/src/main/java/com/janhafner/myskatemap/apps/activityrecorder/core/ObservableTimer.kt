package com.janhafner.myskatemap.apps.activityrecorder.core

import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import org.joda.time.MutablePeriod
import org.joda.time.Period
import org.joda.time.PeriodType
import java.util.*

public final class ObservableTimer : IObservableTimer {
    private val timer: Timer = Timer()

    private var timerTask: TimerTask? = this.createTimerTask()

    private val elapsedSeconds: MutablePeriod = MutablePeriod(PeriodType.time())

    private val secondElapsedSubject: BehaviorSubject<Period> = BehaviorSubject.createDefault<Period>(Period.ZERO)
    public override val secondElapsed: Observable<Period> = this.secondElapsedSubject

    public override val secondsElapsed: Period
        get() = this.secondElapsedSubject.value!!

    private var isRunning: Boolean = false

    private fun createTimerTask(): TimerTask {
        return object: TimerTask() {
            override fun run() {
                val self = this@ObservableTimer

                self.elapsedSeconds.addSeconds(1)

                self.secondElapsedSubject.onNext(self.elapsedSeconds.toPeriod())
            }
        }
    }

    public override fun set(elapsedSecondsSinceStart: Int) {
        if(this.isDestroyed) {
            throw ObjectDestroyedException()
        }

        if (elapsedSecondsSinceStart < 0) {
            throw IllegalArgumentException("elapsedSecondsSinceStart")
        }

        this.elapsedSeconds.clear()
        this.elapsedSeconds.addSeconds(elapsedSecondsSinceStart)

        if (!this.isRunning) {
            this.secondElapsedSubject.onNext(this.elapsedSeconds.toPeriod())
        }
    }

    public override fun reset() {
        this.set(0)
    }

    public override fun start() {
        if(this.isDestroyed) {
            throw ObjectDestroyedException()
        }

        if (this.isRunning) {
            throw IllegalStateException("Timer is already started!")
        }

        if (this.timerTask == null) {
            this.timerTask = this.createTimerTask()
        }

        this.isRunning = true

        this.timer.scheduleAtFixedRate(this.timerTask, 0, 1000)
    }

    public override fun stop() {
        if(this.isDestroyed) {
            throw ObjectDestroyedException()
        }

        if (!this.isRunning) {
            throw IllegalStateException("Timer is already stopped!")
        }

        this.timerTask!!.cancel()
        this.timerTask = null

        this.isRunning = false
    }

    private var isDestroyed: Boolean = false
    public override fun destroy() {
        if(this.isDestroyed) {
            return
        }

        if(this.isRunning) {
            this.stop()
        }

        this.timerTask?.cancel()
        this.timer.cancel()

        this.secondElapsedSubject.onComplete()

        this.reset()

        this.isDestroyed = true
    }
}