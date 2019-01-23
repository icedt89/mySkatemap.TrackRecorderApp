package com.janhafner.myskatemap.apps.trackrecorder.views.activities.viewfinishedtrack.overview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.common.types.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.conversion.distance.IDistanceConverterFactory
import com.janhafner.myskatemap.apps.trackrecorder.conversion.speed.ISpeedConverterFactory
import com.janhafner.myskatemap.apps.trackrecorder.getApplicationInjector
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.viewfinishedtrack.INeedInputTrackRecording
import javax.inject.Inject


internal final class OverviewTabFragment: Fragment(), INeedInputTrackRecording {
    private lateinit var presenter: OverviewTabFragmentPresenter

    @Inject
    public lateinit var appSettings: IAppSettings

    @Inject
    public lateinit var distanceConverterFactory: IDistanceConverterFactory

    @Inject
    public lateinit var speedConverterFactory: ISpeedConverterFactory

    public override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_overview_tab, container, false)
    }

    public override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        this.context!!.getApplicationInjector().inject(this)

        super.onViewCreated(view, savedInstanceState)

        this.presenter = OverviewTabFragmentPresenter(this, this.speedConverterFactory, this.distanceConverterFactory, this.appSettings)
    }

    public override fun setTrackRecording(trackRecording: TrackRecording) {
        this.presenter.setTrackRecording(trackRecording)
    }
}