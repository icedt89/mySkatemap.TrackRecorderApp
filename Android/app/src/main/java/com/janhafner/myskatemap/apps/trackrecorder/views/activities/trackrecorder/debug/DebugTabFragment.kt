package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.debug

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.getApplicationInjector
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance.IDistanceConverterFactory
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.energy.IEnergyConverterFactory
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.speed.ISpeedConverterFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.activitydetection.IActivityDetectorBroadcastReceiverFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.locationavailability.ILocationAvailabilityChangedDetector
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.IServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import javax.inject.Inject

internal final class DebugTabFragment : Fragment() {
    private var presenter: DebugTabFragmentPresenter? = null

    @Inject
    public lateinit var appSettings: IAppSettings

    @Inject
    public lateinit var distanceConverterFactory: IDistanceConverterFactory

    @Inject
    public lateinit var energyConverterFactory: IEnergyConverterFactory

    @Inject
    public lateinit var speedConverterFactory: ISpeedConverterFactory

    @Inject
    public lateinit var trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>

    @Inject
    public lateinit var locationAvailabilityChangedDetector: ILocationAvailabilityChangedDetector

    @Inject
    public lateinit var activityDetectorBroadcastReceiverFactory: IActivityDetectorBroadcastReceiverFactory

    public override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_debug_tab, container, false)
    }

    public override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        this.context!!.getApplicationInjector().inject(this)

        super.onViewCreated(view, savedInstanceState)

        this.presenter = DebugTabFragmentPresenter(this, this.trackRecorderServiceController, this.appSettings, this.distanceConverterFactory,
                this.energyConverterFactory,
                this.speedConverterFactory, this.activityDetectorBroadcastReceiverFactory, this.locationAvailabilityChangedDetector)
    }

    public override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

        if(this.presenter != null) {
            this.presenter!!.setUserVisibleHint(isVisibleToUser)
        }
    }

    public override fun onDestroyView() {
        this.presenter!!.destroy()

        super.onDestroyView()
    }
}

