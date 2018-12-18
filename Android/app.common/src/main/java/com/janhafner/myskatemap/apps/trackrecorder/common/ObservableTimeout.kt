package com.janhafner.myskatemap.apps.trackrecorder.common

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import java.util.*
import java.util.concurrent.TimeUnit

public final class ObservableTimeout(private val timeout: Long, private val timeUnit: TimeUnit = TimeUnit.MILLISECONDS) : IObservableTimeout {
    private val timer: Timer = Timer()

    private var timerTask: TimerTask? = this.createTimerTask()

    private val timedOutSubject: Subject<Unit> = PublishSubject.create()
    public override val timedOut: Observable<Unit> = this.timedOutSubject.subscribeOn(Schedulers.computation())

    private fun createTimerTask(): TimerTask {
        return object: TimerTask() {
            override fun run() {
                val self = this@ObservableTimeout

                self.timedOutSubject.onNext(Unit)

                self.timerTask = null
            }
        }
    }

    public override fun restart() {
        if(this.isDestroyed) {
            throw ObjectDestroyedException()
        }

        this.timerTask?.cancel()
        this.timerTask = null

        if (this.timerTask == null) {
            this.timerTask = this.createTimerTask()
        }

        val timeout = this.timeUnit.toMillis(this.timeout)

        this.timer.schedule(this.timerTask, timeout)
    }

    public override fun stop() {
        if(this.isDestroyed) {
            throw ObjectDestroyedException()
        }

        this.timerTask?.cancel()
        this.timerTask = null
    }

    private var isDestroyed: Boolean = false
    public override fun destroy() {
        if(this.isDestroyed) {
            return
        }

        this.timerTask?.cancel()
        this.timer.cancel()

        this.timedOutSubject.onComplete()

        this.isDestroyed = true
    }
}