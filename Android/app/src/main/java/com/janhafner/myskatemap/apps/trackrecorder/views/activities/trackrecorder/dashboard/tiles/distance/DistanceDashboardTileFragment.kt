package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.distance

import android.os.Bundle
import android.view.View
import com.janhafner.myskatemap.apps.trackrecorder.getApplicationInjector
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance.IDistanceConverterFactory
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.DashboardTileFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.DashboardTileFragmentPresenter
import javax.inject.Inject

internal final class DistanceDashboardTileFragment : DashboardTileFragment() {
    @Inject
    public lateinit var distanceConverterFactory: IDistanceConverterFactory

    public override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        this.context!!.getApplicationInjector().inject(this)

        super.onViewCreated(view, savedInstanceState)
    }

    protected override fun createPresenter(): DashboardTileFragmentPresenter {
        return DistanceDashboardTileFragmentPresenter(this, this.appSettings, this.trackRecorderServiceController, this.distanceConverterFactory)
    }
}