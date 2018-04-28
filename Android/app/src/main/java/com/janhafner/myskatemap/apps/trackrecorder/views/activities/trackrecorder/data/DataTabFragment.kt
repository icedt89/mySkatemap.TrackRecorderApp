package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.data

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.getApplicationInjector
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance.ITrackDistanceUnitFormatterFactory
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.ServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import javax.inject.Inject

internal final class DataTabFragment : Fragment() {
    private lateinit var presenter: DataTabFragmentPresenter

    @Inject
    public lateinit var appSettings: IAppSettings

    @Inject
    public lateinit var trackRecorderUnitFormatterFactory: ITrackDistanceUnitFormatterFactory

    @Inject
    public lateinit var trackRecorderServiceController: ServiceController<TrackRecorderServiceBinder>

    public override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_data_tab, container, false)
    }

    public override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        this.context!!.getApplicationInjector().inject(this)

        super.onViewCreated(view, savedInstanceState)

        this.presenter = DataTabFragmentPresenter(this, this.trackRecorderServiceController, this.appSettings, this.trackRecorderUnitFormatterFactory)
    }

    public override fun onDestroyView() {
        super.onDestroyView()

        this.presenter.destroy()
    }
}