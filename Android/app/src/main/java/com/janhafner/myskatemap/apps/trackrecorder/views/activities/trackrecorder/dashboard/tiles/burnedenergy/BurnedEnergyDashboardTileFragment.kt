package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.burnedenergy

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.view.View
import com.janhafner.myskatemap.apps.trackrecorder.formatting.distance.IDistanceUnitFormatterFactory
import com.janhafner.myskatemap.apps.trackrecorder.formatting.energy.IEnergyUnitFormatterFactory
import com.janhafner.myskatemap.apps.trackrecorder.getApplicationInjector
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.DashboardTileFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.DashboardTileFragmentPresenter
import javax.inject.Inject

internal final class BurnedEnergyDashboardTileFragment : DashboardTileFragment() {
    @Inject
    public lateinit var energyUnitFormatterFactory: IEnergyUnitFormatterFactory

    public override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        this.context!!.getApplicationInjector().inject(this)

        super.onViewCreated(view, savedInstanceState)
    }

    protected override fun createPresenter(): DashboardTileFragmentPresenter {
        return BurnedEnergyDashboardTileFragmentPresenter(this, this.appSettings, this.trackRecorderServiceController, this.energyUnitFormatterFactory)
    }
}