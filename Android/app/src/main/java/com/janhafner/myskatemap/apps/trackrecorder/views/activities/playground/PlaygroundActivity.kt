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
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.setContentView(R.layout.activity_playground)
    }
}

