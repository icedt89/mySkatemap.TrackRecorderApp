package com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.dashboard

import android.view.View
import androidx.lifecycle.Lifecycle
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.dashboard.tiles.DashboardTileFragment
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.dashboard.tiles.FormattedDisplayValue
import com.uber.autodispose.AutoDispose
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activityrecorder_dashboard_tile_default_fragment.*

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
                source
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnDispose {
                            this.connectedDashboardTileFragment = null
                        }
                        .`as`(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(dashboardTileFragment, Lifecycle.Event.ON_DESTROY)))
                        .subscribe {
                            dashboardTileFragment.fragment_dashboard_tile_value.text = it.value
                            dashboardTileFragment.fragment_dashboard_tile_unit.text = it.unit
                        }
        )
    }
}