package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.data

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.getApplicationInjector
import com.janhafner.myskatemap.apps.trackrecorder.services.distance.ITrackDistanceUnitFormatterFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.ServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderService
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import javax.inject.Inject

internal final class DataTabFragment : Fragment() {
    private var presenter: DataTabFragmentPresenter? = null

    @Inject
    public lateinit var appSettings: IAppSettings

    @Inject
    public lateinit var trackRecorderUnitFormatterFactory: ITrackDistanceUnitFormatterFactory

    @Inject
    public lateinit var trackRecorderServiceController: ServiceController<TrackRecorderService, TrackRecorderServiceBinder>

    public override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_data_tab, container, false)
    }

    public override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        this.context!!.getApplicationInjector().inject(this)

        super.onViewCreated(view, savedInstanceState)

        this.presenter = DataTabFragmentPresenter(this, this.trackRecorderServiceController, this.appSettings, this.trackRecorderUnitFormatterFactory)
    }

    public override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

        if(this.presenter != null) {
            this.presenter!!.setUserVisibleHint(isVisibleToUser)
        }
    }

    public override fun onDestroyView() {
        super.onDestroyView()

        this.presenter!!.destroy()
    }
}