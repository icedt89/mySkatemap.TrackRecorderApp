package com.janhafner.myskatemap.apps.trackrecorder.views.activities.playground

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.janhafner.myskatemap.apps.trackrecorder.getApplicationInjector

internal final class PlaygroundActivity : AppCompatActivity() {
    private lateinit var presenter: PlaygroundActivityPresenter

    public override fun onCreate(savedInstanceState: Bundle?) {
        this.getApplicationInjector().inject(this)

        super.onCreate(savedInstanceState)

        this.presenter = PlaygroundActivityPresenter(this)
    }

    public override fun onDestroy() {
        this.presenter.destroy()

        super.onDestroy()
    }
}

