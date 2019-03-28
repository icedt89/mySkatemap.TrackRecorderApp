package com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.dashboard.tiles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.janhafner.myskatemap.apps.activityrecorder.R

internal final class DefaultDashboardTileFragment : DashboardTileFragment() {
    public override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activityrecorder_dashboard_tile_default_fragment, container, false)
    }

    public override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}