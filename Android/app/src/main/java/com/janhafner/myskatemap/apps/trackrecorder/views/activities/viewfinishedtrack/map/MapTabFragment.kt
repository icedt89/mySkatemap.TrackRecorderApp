package com.janhafner.myskatemap.apps.trackrecorder.views.activities.viewfinishedtrack.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.common.types.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.getApplicationInjector
import com.janhafner.myskatemap.apps.trackrecorder.map.TrackRecorderMapFragment
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.viewfinishedtrack.INeedInputTrackRecording
import javax.inject.Inject


internal final class MapTabFragment: Fragment(), INeedInputTrackRecording {
    private lateinit var presenter: MapTabFragmentPresenter

    @Inject
    public lateinit var trackRecorderMapFragment: TrackRecorderMapFragment

    @Inject
    public lateinit var appSettings: IAppSettings

    public override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_viewfinishedtrack_map_tab, container, false)
    }

    public override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        this.context!!.getApplicationInjector().inject(this)

        super.onViewCreated(view, savedInstanceState)

        this.presenter = MapTabFragmentPresenter(this, this.trackRecorderMapFragment, this.appSettings)
    }

    public override fun setTrackRecording(trackRecording: TrackRecording) {
        this.presenter.setTrackRecording(trackRecording)
    }
}