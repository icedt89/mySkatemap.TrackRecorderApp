package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.speed

import android.annotation.SuppressLint
import android.support.annotation.LayoutRes
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.DashboardTileFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.DashboardTileFragmentPresenter

@SuppressLint("ValidFragment")
internal final class CurrentSpeedDashboardTileFragment(@LayoutRes layout: Int) : DashboardTileFragment(layout) {
    protected override fun createPresenter(): DashboardTileFragmentPresenter {
        return CurrentSpeedDashboardTileFragmentPresenter(this, this.appSettings, this.trackRecorderServiceController)
    }
}

