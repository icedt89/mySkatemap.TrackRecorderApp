package com.janhafner.myskatemap.apps.trackrecorder.views.activities.tracklist

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.janhafner.myskatemap.apps.trackrecorder.common.eventing.INotifier
import com.janhafner.myskatemap.apps.trackrecorder.conversion.distance.IDistanceConverterFactory
import com.janhafner.myskatemap.apps.trackrecorder.getApplicationInjector
import com.janhafner.myskatemap.apps.trackrecorder.services.track.ITrackQueryService
import com.janhafner.myskatemap.apps.trackrecorder.services.track.ITrackService
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import javax.inject.Inject


internal final class TrackListActivity: AppCompatActivity(){
    @Inject
    public lateinit var trackQueryService: ITrackQueryService

    @Inject
    public lateinit var trackService: ITrackService

    @Inject
    public lateinit var distanceConverterFactory: IDistanceConverterFactory

    @Inject
    public lateinit var appSettings: IAppSettings

    @Inject
    public lateinit var notifier: INotifier

    private lateinit var presenter: TrackListActivityPresenter

    public override fun onCreate(savedInstanceState: Bundle?) {
        this.getApplicationInjector().inject(this)

        super.onCreate(savedInstanceState)

        this.presenter = TrackListActivityPresenter(this, this.trackQueryService, this.distanceConverterFactory, this.appSettings, this.trackService, this.notifier)
    }

    public override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return this.presenter.onOptionsItemSelected(item)
    }

    public override fun onDestroy() {
        this.presenter.destroy()

        super.onDestroy()
    }
}