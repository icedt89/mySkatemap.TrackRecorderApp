package com.janhafner.myskatemap.apps.trackrecorder.activities.trackrecorder

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.janhafner.myskatemap.apps.trackrecorder.R

internal final class MapTabFragment: Fragment(), ITrackRecorderActivityDependantFragment {
    private lateinit var viewModel: TrackRecorderActivityViewModel

    public override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_map_tab, container, false)
    }

    public override fun setViewModel(viewModel: TrackRecorderActivityViewModel) {
        this.viewModel = viewModel
    }
}

