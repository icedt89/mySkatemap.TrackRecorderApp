package com.janhafner.myskatemap.apps.trackrecorder.infrastructure

import android.os.SystemClock
import android.util.Log
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

    private val secondElapsedSubject : BehaviorSubject<Period> = BehaviorSubject.createDefault<Period>(Period.ZERO)
    public val secondElapsed : Observable<Period> = this.secondElapsedSubject.share()

    private val isRunningSubject: BehaviorSubject<Boolean> = BehaviorSubject.createDefault<Boolean>(false)
    public val isRunningChanged: Observable<Boolean> = this.isRunningSubject.share()

    private val timerResetSubject : Subject<Long> = PublishSubject.create<Long>()
    public val timerReset : Observable<Long> = this.timerResetSubject.share()

    private val elapsedSeconds : MutablePeriod = MutablePeriod(PeriodType.seconds())

    private fun createTimerTask() : TimerTask {
        return object : TimerTask() {
            override fun run() {
                val self = this@ObservableTimer

                self.elapsedSeconds.addSeconds(1)

                Log.v("ObservableTimer", "One more second is elapsed, total duration is ${self.elapsedSeconds.seconds}")

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

        Log.v("ObservableTimer", "Elapsed seconds reset to ${elapsedSecondsSinceStart}")

        this.timerResetSubject.onNext(SystemClock.elapsedRealtime())
    }

    public fun reset() {
        this.reset(0)
    }

    public fun start() {
        if(this.isRunning) {
            throw IllegalStateException()
        }

        if(this.timerTask == null) {
            this.timerTask = this.createTimerTask()
        }

        this.changeState(true)

        this.timer.scheduleAtFixedRate(this.timerTask, 0, 1000)
    }

    public fun stop() {
        if(!this.isRunning) {
            throw IllegalStateException()
        }

        this.timerTask!!.cancel()
        this.timerTask = null

        this.changeState(false)
    }

    private fun changeState(isRunning : Boolean) {
        this.isRunning = isRunning

        this.isRunningSubject.onNext(isRunning)
    }
}