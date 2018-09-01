package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.debug

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.janhafner.myskatemap.apps.trackrecorder.*
import com.janhafner.myskatemap.apps.trackrecorder.formatting.burnedenergy.IBurnedEnergyUnitFormatterFactory
import com.janhafner.myskatemap.apps.trackrecorder.formatting.distance.IDistanceUnitFormatterFactory
import com.janhafner.myskatemap.apps.trackrecorder.formatting.speed.ISpeedUnitFormatterFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.stilldetection.IStillDetector
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.ILocationAvailabilityChangedDetector
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.IServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.LocationAvailabilityChangedBroadcastReceiver
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import javax.inject.Inject

internal final class DebugTabFragment : Fragment() {
    private var presenter: DebugTabFragmentPresenter? = null

    @Inject
    public lateinit var appSettings: IAppSettings

    @Inject
    public lateinit var trackRecorderUnitFormatterFactory: IDistanceUnitFormatterFactory

    @Inject
    public lateinit var burnedEnergyUnitFormatterFactory: IBurnedEnergyUnitFormatterFactory

    @Inject
    public lateinit var speedUnitFormatterFactory: ISpeedUnitFormatterFactory

    @Inject
    public lateinit var stillDetector: IStillDetector

    @Inject
    public lateinit var trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>

    @Inject
    public lateinit var locationAvailabilityChangedDetector: ILocationAvailabilityChangedDetector

    public override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_debug_tab, container, false)
    }

    public override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        this.context!!.getApplicationInjector().inject(this)

        super.onViewCreated(view, savedInstanceState)

        this.presenter = DebugTabFragmentPresenter(this, this.trackRecorderServiceController, this.appSettings, this.trackRecorderUnitFormatterFactory,
                this.burnedEnergyUnitFormatterFactory,
                this.speedUnitFormatterFactory, this.stillDetector, this.locationAvailabilityChangedDetector)
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

