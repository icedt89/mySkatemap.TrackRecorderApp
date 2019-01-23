package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard

import android.view.View
import com.jakewharton.rxbinding2.widget.text
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.DashboardTileFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.FormattedDisplayValue
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_dashboard_tile_default.*

internal class TextOnlyDashboardTileFragmentPresenterConnector : IDashboardTileFragmentPresenterConnector {
    private var connectedDashboardTileFragment: DashboardTileFragment? = null

    public override fun connect(dashboardTileFragment: DashboardTileFragment, source: Observable<FormattedDisplayValue>): List<Disposable> {
        if (this.connectedDashboardTileFragment != dashboardTileFragment) {
            dashboardTileFragment.fragment_dashboard_tile_unit.visibility = View.VISIBLE
            dashboardTileFragment.fragment_dashboard_tile_value.visibility = View.VISIBLE

            dashboardTileFragment.fragment_dashboard_tile_line_chart.visibility = View.GONE

            this.connectedDashboardTileFragment = dashboardTileFragment
        }

        return listOf(
                source.map {
                    it.value
                }
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(dashboardTileFragment.fragment_dashboard_tile_value.text()),
                source.map {
                    it.unit
                }
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(dashboardTileFragment.fragment_dashboard_tile_unit.text())
        )
    }
}