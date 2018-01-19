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

    public var state : ObservableTimerState = ObservableTimerState.Stopped
        private set

    private val secondElapsedSubject : BehaviorSubject<Period> = BehaviorSubject.createDefault<Period>(Period.ZERO)
    public val secondElapsed : Observable<Period> = this.secondElapsedSubject.share()

    private val stateChangedSubject : BehaviorSubject<ObservableTimerState> = BehaviorSubject.createDefault<ObservableTimerState>(ObservableTimerState.Stopped)
    public val stateChanged : Observable<ObservableTimerState> = this.stateChangedSubject.share()

    private val timerResetSubject : Subject<Long> = PublishSubject.create<Long>()
    public val timerReset : Observable<Long> = this.timerResetSubject.share()

    private val elapsedSeconds : MutablePeriod = MutablePeriod(PeriodType.seconds())

    private fun createTimerTask() : TimerTask {
        return object : TimerTask() {
            override fun run() {
                val self = this@ObservableTimer

                self.elapsedSeconds.addSeconds(1)

                self.secondElapsedSubject.onNext(self.elapsedSeconds.toPeriod())
            }
        }
    }

    public fun reset(elapsedSecondsSinceStart : Int) {
        if(elapsedSecondsSinceStart < 0) {
            throw IllegalArgumentException("elapsedSecondsSinceStart")
        }

        this.elapsedSeconds.clear()
        this.elapsedSeconds.addSeconds(elapsedSecondsSinceStart)

        this.timerResetSubject.onNext(SystemClock.elapsedRealtime())
    }

    public fun reset() {
        this.reset(0)
    }

    public fun start() {
        if(this.state == ObservableTimerState.Running) {
            throw IllegalStateException()
        }

        if(this.timerTask == null) {
            this.timerTask = this.createTimerTask()
        }

        this.changeState(ObservableTimerState.Running)

        this.timer.scheduleAtFixedRate(this.timerTask, 0, 1000)
    }

    public fun stop() {
        if(this.state == ObservableTimerState.Stopped) {
            throw IllegalStateException()
        }

        this.timerTask!!.cancel()
        this.timerTask = null

        this.changeState(ObservableTimerState.Stopped)
    }

    private fun changeState(state : ObservableTimerState) {
        this.state = state

        this.stateChangedSubject.onNext(state)
    }
}