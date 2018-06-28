package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.ambienttemperature

import android.annotation.SuppressLint
import android.support.annotation.LayoutRes
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.DashboardTileFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.DashboardTileFragmentPresenter

@SuppressLint("ValidFragment")
internal final class MaximumAmbientTemperatureDashboardTileFragment(@LayoutRes layout: Int) : DashboardTileFragment(layout) {
    protected override fun createPresenter(): DashboardTileFragmentPresenter {
        return MaximumAmbientTemperatureDashboardTileFragmentPresenter(this, this.appSettings, this.trackRecorderServiceController)
    }
}