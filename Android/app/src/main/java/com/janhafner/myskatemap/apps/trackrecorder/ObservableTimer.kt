package com.janhafner.myskatemap.apps.trackrecorder

import android.os.SystemClock
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import org.joda.time.MutablePeriod
import org.joda.time.Period
import org.joda.time.PeriodType
import java.util.*

internal final class ObservableTimer : IObservableTimer {
    private val timer: Timer = Timer()

    private var timerTask: TimerTask? = this.createTimerTask()

    private val elapsedSeconds: MutablePeriod = MutablePeriod(PeriodType.time())

    private val secondElapsedSubject: BehaviorSubject<Period> = BehaviorSubject.createDefault<Period>(Period.ZERO)
    public override val secondElapsed: Observable<Period> = this.secondElapsedSubject.subscribeOn(Schedulers.computation())

    public override val secondsElapsed: Period
        get() = this.secondElapsedSubject.value

    private val isRunningChangedSubject: BehaviorSubject<Boolean> = BehaviorSubject.createDefault<Boolean>(false)
    public override val isRunningChanged: Observable<Boolean> = this.isRunningChangedSubject.subscribeOn(Schedulers.computation())

    public override val isRunning: Boolean
        get() = this.isRunningChangedSubject.value

    private val timerResetSubject: Subject<Long> = PublishSubject.create<Long>()
    public override val timerReset: Observable<Long> = this.timerResetSubject.subscribeOn(Schedulers.computation())

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
            throw IllegalStateException("Object is destroyed!")
        }

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

    public override fun reset() {
        this.set(0)
    }

    public override fun start() {
        if(this.isDestroyed) {
            throw IllegalStateException("Object is destroyed!")
        }

        if (this.isRunning) {
            throw IllegalStateException("Timer is already started!")
        }

        if (this.timerTask == null) {
            this.timerTask = this.createTimerTask()
        }

        this.isRunningChangedSubject.onNext(true)

        this.timer.scheduleAtFixedRate(this.timerTask, 0, 1000)
    }

    public override fun stop() {
        if(this.isDestroyed) {
            throw IllegalStateException("Object is destroyed!")
        }

        if (!this.isRunning) {
            throw IllegalStateException("Timer is already stopped!")
        }

        this.timerTask!!.cancel()
        this.timerTask = null

        this.isRunningChangedSubject.onNext(false)
    }

    private var isDestroyed: Boolean = false
    public override fun destroy() {
        if(this.isDestroyed) {
            return
        }

        if(this.isRunning) {
            this.stop()
        }

        this.reset()
        this.timerTask?.cancel()
        this.timer.cancel()

        this.isRunningChangedSubject.onComplete()
        this.secondElapsedSubject.onComplete()
        this.timerResetSubject.onComplete()

        this.isDestroyed = true
    }
}