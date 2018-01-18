package com.janhafner.myskatemap.apps.trackrecorder.infrastructure

import android.os.SystemClock
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import org.joda.time.Duration
import java.util.*

internal final class ObservableTimer {
    public var state : ObservableTimerState = ObservableTimerState.Stopped
        private set

    private var elapsedSecondsSinceStart : Long = 0

    private val timer: Timer = Timer()

    private var timerTask: TimerTask? = this.createTimerTask()

    private fun createTimerTask() : TimerTask {
        return object : TimerTask() {
            override fun run() {
                val self = this@ObservableTimer

                self.secondElapsedSubject.onNext(Duration.standardSeconds(self.elapsedSecondsSinceStart++))
            }
        }
    }

    public fun reset(elapsedSecondsSinceStart : Long) {
        this.elapsedSecondsSinceStart = elapsedSecondsSinceStart

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

    private val secondElapsedSubject : BehaviorSubject<Duration> = BehaviorSubject.createDefault<Duration>(Duration.ZERO)
    public val secondElapsed : Observable<Duration>
        get() = this.secondElapsedSubject

    private val stateChangedSubject : BehaviorSubject<ObservableTimerState> = BehaviorSubject.createDefault<ObservableTimerState>(ObservableTimerState.Stopped)
    public val stateChanged : Observable<ObservableTimerState>
        get() = this.stateChangedSubject

    private val timerResetSubject : Subject<Long> = PublishSubject.create<Long>()
    public val timerReset : Observable<Long>
        get() = this.timerResetSubject
}