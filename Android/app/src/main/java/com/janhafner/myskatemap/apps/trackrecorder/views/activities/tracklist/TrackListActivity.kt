package com.janhafner.myskatemap.apps.trackrecorder.views.activities.tracklist

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.janhafner.myskatemap.apps.trackrecorder.getApplicationInjector
import com.janhafner.myskatemap.apps.trackrecorder.services.track.ITrackQueryService
import javax.inject.Inject


internal final class TrackListActivity: AppCompatActivity(){
    @Inject
    public lateinit var trackQueryService: ITrackQueryService

    private var presenter: TrackListActivityPresenter? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        this.getApplicationInjector().inject(this)

        super.onCreate(savedInstanceState)

        this.presenter = TrackListActivityPresenter(this, this.trackQueryService)
    }

    public override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return this.presenter!!.onOptionsItemSelected(item)
    }

    public override fun onDestroy() {
        this.presenter!!.destroy()

        super.onDestroy()
    }
}