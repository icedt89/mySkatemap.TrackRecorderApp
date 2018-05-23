package com.janhafner.myskatemap.apps.trackrecorder.views.activities.tracklist

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.janhafner.myskatemap.apps.trackrecorder.getApplicationInjector
import com.janhafner.myskatemap.apps.trackrecorder.services.ITrackService
import com.janhafner.myskatemap.apps.trackrecorder.services.calories.MetActivityDefinitionFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.distance.ITrackDistanceUnitFormatterFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.distance.TrackDistanceCalculator
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import javax.inject.Inject

internal final class TrackListActivity : AppCompatActivity() {
    @Inject
    public lateinit var trackService: ITrackService

    @Inject
    public lateinit var appSettings: IAppSettings

    @Inject
    public lateinit var trackDistanceUnitFormatterFactory: ITrackDistanceUnitFormatterFactory

    @Inject
    public lateinit var metActivityDefinitionFactory: MetActivityDefinitionFactory

    private lateinit var presenter: TrackListActivityPresenter

    public override fun onCreate(savedInstanceState: Bundle?) {
        this.getApplicationInjector().inject(this)

        super.onCreate(savedInstanceState)

        this.presenter = TrackListActivityPresenter(this,
                this.trackService,
                this.appSettings,
                this.trackDistanceUnitFormatterFactory,
                this.metActivityDefinitionFactory)
    }

    public override fun onDestroy() {
        super.onDestroy()

        this.presenter.destroy()
    }
}