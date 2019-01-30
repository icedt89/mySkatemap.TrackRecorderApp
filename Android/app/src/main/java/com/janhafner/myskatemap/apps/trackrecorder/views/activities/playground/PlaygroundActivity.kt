package com.janhafner.myskatemap.apps.trackrecorder.views.activities.playground

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.trello.rxlifecycle3.android.lifecycle.kotlin.bindUntilEvent
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

internal final class PlaygroundActivity : AppCompatActivity() {
    private val subscriptions = CompositeDisposable()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.setContentView(R.layout.activity_playground)

        val sourceUnitDestroy = Observable.interval(1, TimeUnit.SECONDS, Schedulers.computation())
                .doOnTerminate {
                    Log.e("PA|LC_ON_DESTROY", "ON_DESTROY: LIFECYCLE DESTROYED ME!")
                }
                .doOnDispose {
                    Log.e("PF|LC_ON_DESTROY", "ON_DISPOSE: LIFECYCLE DESTROYED ME!")
                }
                .doOnComplete {
                    Log.e("PF|LC_ON_DESTROY", "ON_COMPLETE: LIFECYCLE DESTROYED ME!")
                }
                .bindUntilEvent(this, Lifecycle.Event.ON_DESTROY)
                .subscribe {
                    Log.i("PA|LC_ON_DESTROY", "ON_DESTROY: IAM ALIVE!")
                }
        val sourceUnitStop = Observable.interval(1, TimeUnit.SECONDS, Schedulers.computation())
                .doOnDispose {
                    Log.e("PA|LC_ON_STOP", "ON_STOP: LIFECYCLE DESTROYED ME!")
                }
                .bindUntilEvent(this, Lifecycle.Event.ON_STOP)
                .subscribe {
                    Log.i("PA|LC_ON_STOP", "ON_STOP: IAM ALIVE!")
                }
        val sourceUnitPause = Observable.interval(1, TimeUnit.SECONDS, Schedulers.computation())
                .doOnDispose {
                    Log.e("PA|LC_ON_PAUSE", "ON_PAUSE: LIFECYCLE DESTROYED ME!")
                }
                .bindUntilEvent(this, Lifecycle.Event.ON_PAUSE)
                .subscribe {
                    Log.i("PA|LC_ON_PAUSE", "ON_PAUSE: IAM ALIVE!")
                }

        this.subscriptions.addAll(
                sourceUnitDestroy,
                sourceUnitStop,
                sourceUnitPause
        )
    }
}

