package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.distance

import android.annotation.SuppressLint
import android.support.annotation.LayoutRes
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.DashboardTileFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.DashboardTileFragmentPresenter

@SuppressLint("ValidFragment")
internal final class DistanceDashboardTileFragment(@LayoutRes layout: Int) : DashboardTileFragment(layout) {
    protected override fun createPresenter(): DashboardTileFragmentPresenter {
        return DistanceDashboardTileFragmentPresenter(this, this.appSettings, this.trackRecorderServiceController)
    }
}