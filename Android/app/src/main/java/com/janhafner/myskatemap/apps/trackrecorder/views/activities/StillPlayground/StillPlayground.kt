package com.janhafner.myskatemap.apps.trackrecorder.views.activities.StillPlayground

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.rxbinding2.view.clicks
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.common.IDestroyable
import com.janhafner.myskatemap.apps.trackrecorder.common.ObjectDestroyedException
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.activity_stillplayground.*
import org.joda.time.DateTime
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

internal final class StillPlaygroundActivity : AppCompatActivity() {
    private val currentValue = AtomicInteger(-1)
    private val locationEmitter: Subject<Int> = PublishSubject.create()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.setContentView(R.layout.activity_stillplayground)

        this.stillplayground_emit_location.clicks().subscribe{
            val newValue = currentValue.incrementAndGet()

            this.locationEmitter.onNext(newValue)
        }

        val displacementInMeters: Double = 3.0
        val displacementTimeInSeconds: Long = 4

        val sourceObservable = this.locationEmitter
                .timestamp()
                .map {
                    Log.i("IS_PG", "${it.value()} emitted at ${DateTime(it.time())}")

                    false
                }
                .debounce(2, TimeUnit.SECONDS)

        val consolidatedObservable = sourceObservable
                .compose(TimeoutTransformer(displacementTimeInSeconds, { true }, TimeUnit.SECONDS))
                .subscribe{
                    if (it) {
                        this.stillplayground_isstill.text = "!!! STILL !!!"
                    } else {
                        this.stillplayground_isstill.text = "<<< NOT STILL >>>"
                    }
                }
    }


    public final class TimeoutTransformer<TStream>(private val timeout: Long, private val mapper: (onTimeout: Unit) -> TStream, private val timeUnit: TimeUnit = TimeUnit.MILLISECONDS): ObservableTransformer<TStream, TStream> {
        private val observableTimeout = ObservableTimeout()

        public override fun apply(upstream: Observable<TStream>): ObservableSource<TStream> {
            return upstream
                    .doOnNext{
                        val timeout = timeUnit.toMillis(timeout)

                        this.observableTimeout.restart(timeout)

                        Log.i("IS_PG", "Timeout of ${timeout}ms reset")
                    }
                    .doOnDispose {
                        this.observableTimeout.destroy()
                    }
                    .mergeWith(this.observableTimeout
                            .timedOut
                            .map(mapper))
        }

        private final class ObservableTimeout : IDestroyable {
            private val timer: Timer = Timer()

            private var timerTask: TimerTask? = this.createTimerTask()

            private val timedOutSubject: Subject<Unit> = PublishSubject.create()
            public val timedOut: Observable<Unit> = this.timedOutSubject.subscribeOn(Schedulers.computation())

            private fun createTimerTask(): TimerTask {
                return object: TimerTask() {
                    override fun run() {
                        val self = this@ObservableTimeout

                        self.timedOutSubject.onNext(Unit)

                        self.timerTask = null

                        Log.i("IS_PG", "Emitting timeout")
                    }
                }
            }

            public fun restart(timeout: Long) {
                if(this.isDestroyed) {
                    throw ObjectDestroyedException()
                }

                this.timerTask?.cancel()
                this.timerTask = null

                if (this.timerTask == null) {
                    this.timerTask = this.createTimerTask()
                }

                this.timer.schedule(this.timerTask, timeout)
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
    }
}