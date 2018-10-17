package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.map

import android.content.Context
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.getApplicationInjector
import com.janhafner.myskatemap.apps.trackrecorder.locationavailability.ILocationAvailabilityChangedSource
import com.janhafner.myskatemap.apps.trackrecorder.locationavailability.LocationAvailabilityChangedBroadcastReceiver
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.IServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider.IMyLocationProvider
import com.janhafner.myskatemap.apps.trackrecorder.views.INeedFragmentVisibilityInfo
import com.janhafner.myskatemap.apps.trackrecorder.views.map.ITrackRecorderMapFragmentFactory
import javax.inject.Inject


internal final class MapTabFragment: Fragment() {
    private var presenter: MapTabFragmentPresenter? = null

    @Inject
    public lateinit var trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>

    @Inject
    public lateinit var trackRecorderMapFragmentFactory: ITrackRecorderMapFragmentFactory

    @Inject
    public lateinit var locationAvailabilityChangedBroadcastReceiver: LocationAvailabilityChangedBroadcastReceiver

    @Inject
    public lateinit var locationAvailabilityChangedSource: ILocationAvailabilityChangedSource

    @Inject
    public lateinit var myLocationProvider: IMyLocationProvider

    public override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_map_tab, container, false)
    }

    public override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

        if(this.presenter != null) {
            this.presenter!!.setUserVisibleHint(isVisibleToUser)
        }
    }

    public override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        this.context!!.getApplicationInjector().inject(this)

        super.onViewCreated(view, savedInstanceState)

        this.presenter = MapTabFragmentPresenter(this, this.trackRecorderServiceController, this.trackRecorderMapFragmentFactory, this.myLocationProvider, this.locationAvailabilityChangedSource)
    }

    public override fun onAttach(context: Context?) {
        super.onAttach(context)

        if(this.activity is INeedFragmentVisibilityInfo) {
            (this.activity as INeedFragmentVisibilityInfo).onFragmentVisibilityChange(this, true)
        }
    }

    public override fun onResume() {
        super.onResume()

        this.activity!!.registerReceiver(this.locationAvailabilityChangedBroadcastReceiver, IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION))

        if(this.presenter != null) {
            this.presenter!!.onResume()
        }
    }

    public override fun onPause() {
        super.onPause()

        this.activity!!.unregisterReceiver(this.locationAvailabilityChangedBroadcastReceiver)

        this.presenter!!.onPause()
    }

    public override fun onDestroyView() {
        this.presenter!!.destroy()

        super.onDestroyView()
    }
}