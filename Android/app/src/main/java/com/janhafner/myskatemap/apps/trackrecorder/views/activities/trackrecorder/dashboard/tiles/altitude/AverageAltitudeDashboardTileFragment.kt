package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.altitude

import android.annotation.SuppressLint
import android.support.annotation.LayoutRes
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.DashboardTileFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.DashboardTileFragmentPresenter


@SuppressLint("ValidFragment")
internal final class AverageAltitudeDashboardTileFragment(@LayoutRes layout: Int) : DashboardTileFragment(layout) {
    protected override fun createPresenter(): DashboardTileFragmentPresenter {
        return AverageAltitudeDashboardTileFragmentPresenter(this, this.appSettings, this.trackRecorderServiceController)
    }
}

