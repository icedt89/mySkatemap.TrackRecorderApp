package com.janhafner.myskatemap.apps.trackrecorder.views.activities.tracklist

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.janhafner.myskatemap.apps.trackrecorder.getApplicationInjector
import com.janhafner.myskatemap.apps.trackrecorder.services.track.ITrackService
import javax.inject.Inject


internal final class TrackListActivity: AppCompatActivity(){
    @Inject
    public lateinit var trackService: ITrackService

    private var presenter: TrackListActivityPresenter? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        this.getApplicationInjector().inject(this)

        super.onCreate(savedInstanceState)

        this.presenter = TrackListActivityPresenter(this, this.trackService)
    }

    public override fun onDestroy() {
        this.presenter!!.destroy()

        super.onDestroy()
    }
}