package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.burnedenergy

import android.os.Bundle
import android.view.View
import com.janhafner.myskatemap.apps.trackrecorder.getApplicationInjector
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.energy.IEnergyConverterFactory
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.DashboardTileFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.DashboardTileFragmentPresenter
import javax.inject.Inject

internal final class BurnedEnergyDashboardTileFragment : DashboardTileFragment() {
    @Inject
    public lateinit var energyConverterFactory: IEnergyConverterFactory

    public override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        this.context!!.getApplicationInjector().inject(this)

        super.onViewCreated(view, savedInstanceState)
    }

    protected override fun createPresenter(): DashboardTileFragmentPresenter {
        return BurnedEnergyDashboardTileFragmentPresenter(this, this.appSettings, this.trackRecorderServiceController, this.energyConverterFactory)
    }
}