package com.janhafner.myskatemap.apps.trackrecorder.views.activities.tracklist

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.janhafner.myskatemap.apps.trackrecorder.getApplicationInjector
import com.janhafner.myskatemap.apps.trackrecorder.services.ITrackService
import com.janhafner.myskatemap.apps.trackrecorder.services.calories.IMetActivityDefinitionFactory
import com.janhafner.myskatemap.apps.trackrecorder.formatting.distance.IDistanceUnitFormatterFactory
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import javax.inject.Inject

internal final class TrackListActivity : AppCompatActivity() {
    @Inject
    public lateinit var trackService: ITrackService

    @Inject
    public lateinit var appSettings: IAppSettings

    @Inject
    public lateinit var distanceUnitFormatterFactory: IDistanceUnitFormatterFactory

    @Inject
    public lateinit var metActivityDefinitionFactory: IMetActivityDefinitionFactory

    private lateinit var presenter: TrackListActivityPresenter

    public override fun onCreate(savedInstanceState: Bundle?) {
        this.getApplicationInjector().inject(this)

        super.onCreate(savedInstanceState)

        this.presenter = TrackListActivityPresenter(this,
                this.trackService,
                this.appSettings,
                this.distanceUnitFormatterFactory,
                this.metActivityDefinitionFactory)
    }

    public override fun onDestroy() {
        super.onDestroy()

        this.presenter.destroy()
    }
}