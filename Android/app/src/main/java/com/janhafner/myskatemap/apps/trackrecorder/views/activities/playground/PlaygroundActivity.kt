package com.janhafner.myskatemap.apps.trackrecorder.views.activities.playground

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.janhafner.myskatemap.apps.trackrecorder.getApplicationInjector
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.IServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import javax.inject.Inject

internal final class PlaygroundActivity : AppCompatActivity() {
    @Inject
    public lateinit var trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>

    private lateinit var presenter: PlaygroundActivityPresenter

    public override fun onCreate(savedInstanceState: Bundle?) {
        this.getApplicationInjector().inject(this)

        super.onCreate(savedInstanceState)

        this.presenter = PlaygroundActivityPresenter(this, this.trackRecorderServiceController)
    }

    public override fun onDestroy() {
        this.presenter.destroy()

        super.onDestroy()
    }
}

