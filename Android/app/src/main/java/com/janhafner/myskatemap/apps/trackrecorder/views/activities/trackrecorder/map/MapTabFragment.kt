package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.map

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.getApplicationInjector
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.ServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.views.map.ITrackRecorderMapFragmentFactory
import com.janhafner.myskatemap.apps.trackrecorder.views.map.TrackRecorderMapFragment
import javax.inject.Inject


internal final class MapTabFragment: Fragment() {
    private lateinit var presenter: MapTabFragmentPresenter

    @Inject
    public lateinit var trackRecorderServiceController: ServiceController<TrackRecorderServiceBinder>

    @Inject
    public lateinit var trackRecorderMapFragmentFactory: ITrackRecorderMapFragmentFactory

    public lateinit var trackRecorderMapFragment: TrackRecorderMapFragment

    public override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_map_tab, container, false)
    }

    public override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        this.context!!.getApplicationInjector().inject(this)

        super.onViewCreated(view, savedInstanceState)

        this.trackRecorderMapFragment = this.trackRecorderMapFragmentFactory.getFragment()

        this.childFragmentManager.beginTransaction()
                .replace(R.id.fragment_track_recorder_map_map_placeholder, this.trackRecorderMapFragment)
                .commit()

        this.presenter = MapTabFragmentPresenter(this, this.trackRecorderServiceController)
    }

    public override fun onDestroyView() {
        super.onDestroyView()

        this.presenter.destroy()
    }
}