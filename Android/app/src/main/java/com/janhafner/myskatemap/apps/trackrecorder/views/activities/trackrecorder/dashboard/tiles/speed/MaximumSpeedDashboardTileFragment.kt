package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.speed

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.view.View
import com.janhafner.myskatemap.apps.trackrecorder.formatting.speed.ISpeedUnitFormatterFactory
import com.janhafner.myskatemap.apps.trackrecorder.getApplicationInjector
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.DashboardTileFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.DashboardTileFragmentPresenter
import javax.inject.Inject

internal final class MaximumSpeedDashboardTileFragment : DashboardTileFragment() {
    @Inject
    public lateinit var speedUnitFormatterFactory: ISpeedUnitFormatterFactory

    public override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        this.context!!.getApplicationInjector().inject(this)

        super.onViewCreated(view, savedInstanceState)
    }

    protected override fun createPresenter(): DashboardTileFragmentPresenter {
        return MaximumSpeedDashboardTileFragmentPresenter(this, this.appSettings, this.trackRecorderServiceController, this.speedUnitFormatterFactory)
    }
}