package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard

import android.widget.Toast
import com.jakewharton.rxbinding2.view.longClicks
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.common.types.Dashboard
import com.janhafner.myskatemap.apps.trackrecorder.findChildFragmentById
import com.janhafner.myskatemap.apps.trackrecorder.services.dashboard.IDashboardService
import com.janhafner.myskatemap.apps.trackrecorder.views.INeedFragmentVisibilityInfo
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.*
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.altitude.AverageAltitudeDashboardTileFragmentPresenter
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.altitude.CurrentAltitudeDashboardTileFragmentPresenter
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.altitude.MaximumAltitudeDashboardTileFragmentPresenter
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.altitude.MinimumAltitudeDashboardTileFragmentPresenter
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.speed.AverageSpeedDashboardTileFragmentPresenter
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.speed.CurrentSpeedDashboardTileFragmentPresenter
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.speed.MaximumSpeedDashboardTileFragmentPresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers


internal final class DashboardTabFragmentPresenter(private val view: DashboardTabFragment,
                                                   private val dashboardService: IDashboardService,
                                                   private val dashboardTileFragmentPresenterFactory: IDashboardTileFragmentPresenterFactory) {
    private val subscriptions: CompositeDisposable = CompositeDisposable()
    init {
        this.dashboardService.getCurrentDashboardOrDefault()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .map {
                    val topLeftFragmentPresenter = this.dashboardTileFragmentPresenterFactory.createPresenterFromTypeName(it.topLeftTileImplementationTypeName)
                    val topRightFragmentPresenter = this.dashboardTileFragmentPresenterFactory.createPresenterFromTypeName(it.topRightTileImplementationTypeName)
                    val middleCenterFragmentPresenter = this.dashboardTileFragmentPresenterFactory.createPresenterFromTypeName(it.middleCenterTileImplementationTypeName)
                    val bottomLeftFragmentPresenter = this.dashboardTileFragmentPresenterFactory.createPresenterFromTypeName(it.bottomLeftTileImplementationTypeName)
                    val bottomRightFragmentPresenter = this.dashboardTileFragmentPresenterFactory.createPresenterFromTypeName(it.bottomRightTileImplementationTypeName)

                    object : Any() {
                        public val dashboard: Dashboard = it

                        public val topLeftFragmentPresenter: DashboardTileFragmentPresenter = topLeftFragmentPresenter

                        public val topRightFragmentPresenter: DashboardTileFragmentPresenter = topRightFragmentPresenter

                        public val middleCenterFragmentPresenter: DashboardTileFragmentPresenter = middleCenterFragmentPresenter

                        public val bottomLeftFragmentPresenter: DashboardTileFragmentPresenter = bottomLeftFragmentPresenter

                        public val bottomRightFragmentPresenter: DashboardTileFragmentPresenter = bottomRightFragmentPresenter
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    dashboardResult, _ ->
                        var topLeftFragment = this.view.findChildFragmentById<DashboardTileFragment>(R.id.trackrecorderactivity_tab_dashboard_tile_top_left)
                        topLeftFragment.presenter = dashboardResult.topLeftFragmentPresenter
                        this.subscriptions.add(topLeftFragment.view!!.longClicks().subscribe{
                            this.changeTileFragmentPresenter(dashboardResult.dashboard, topLeftFragment, "topLeft")
                        })

                        val topRightFragment = this.view.findChildFragmentById<DashboardTileFragment>(R.id.trackrecorderactivity_tab_dashboard_tile_top_right)
                        topRightFragment.presenter = dashboardResult.topRightFragmentPresenter
                        this.subscriptions.add(topRightFragment.view!!.longClicks().subscribe{
                            this.changeTileFragmentPresenter(dashboardResult.dashboard, topRightFragment, "topRight")
                        })

                        val middleCenterFragment = this.view.findChildFragmentById<DashboardTileFragment>(R.id.trackrecorderactivity_tab_dashboard_tile_middle_center)
                        middleCenterFragment.presenter = dashboardResult.middleCenterFragmentPresenter

                        val bottomLeftFragment = this.view.findChildFragmentById<DashboardTileFragment>(R.id.trackrecorderactivity_tab_dashboard_tile_bottom_left)
                        bottomLeftFragment.presenter = dashboardResult.bottomLeftFragmentPresenter
                        this.subscriptions.add(bottomLeftFragment.view!!.longClicks().subscribe{
                            this.changeTileFragmentPresenter(dashboardResult.dashboard, bottomLeftFragment, "bottomLeft")
                        })

                        val bottomRightFragment = this.view.findChildFragmentById<DashboardTileFragment>(R.id.trackrecorderactivity_tab_dashboard_tile_bottom_right)
                        bottomRightFragment.presenter = dashboardResult.bottomRightFragmentPresenter
                        this.subscriptions.add(bottomRightFragment.view!!.longClicks().subscribe{
                            this.changeTileFragmentPresenter(dashboardResult.dashboard, bottomRightFragment, "bottomRight")
                        })
                }
    }

    private fun changeTileFragmentPresenter(dashboard: Dashboard, dashboardTileFragment: DashboardTileFragment, position: String) {
        val currentDashbordTileFragmentPresenter = dashboardTileFragment.presenter!!

        var newDashboardTileFragmentPresenter: DashboardTileFragmentPresenter? = null
        when (position) {
            "topLeft" -> { // Distance, Number of locations
                when (currentDashbordTileFragmentPresenter::class.java) {
                    DistanceDashboardTileFragmentPresenter::class.java ->
                        newDashboardTileFragmentPresenter = this.dashboardTileFragmentPresenterFactory.createPresenterFromTypeName(NumberOfLocationsDashboardTileFragmentPresenter::class.java.simpleName)
                    NumberOfLocationsDashboardTileFragmentPresenter::class.java ->
                        newDashboardTileFragmentPresenter = this.dashboardTileFragmentPresenterFactory.createPresenterFromTypeName(DistanceDashboardTileFragmentPresenter::class.java.simpleName)
                }

                if (newDashboardTileFragmentPresenter != null) {
                    dashboard.topLeftTileImplementationTypeName = newDashboardTileFragmentPresenter::class.java.simpleName
                }
            }
            "bottomLeft" -> { // Speed
                when (currentDashbordTileFragmentPresenter::class.java) {
                    CurrentSpeedDashboardTileFragmentPresenter::class.java ->
                        newDashboardTileFragmentPresenter = this.dashboardTileFragmentPresenterFactory.createPresenterFromTypeName(AverageSpeedDashboardTileFragmentPresenter::class.java.simpleName)
                    AverageSpeedDashboardTileFragmentPresenter::class.java ->
                        newDashboardTileFragmentPresenter = this.dashboardTileFragmentPresenterFactory.createPresenterFromTypeName(MaximumSpeedDashboardTileFragmentPresenter::class.java.simpleName)
                    MaximumSpeedDashboardTileFragmentPresenter::class.java ->
                        newDashboardTileFragmentPresenter = this.dashboardTileFragmentPresenterFactory.createPresenterFromTypeName(CurrentSpeedDashboardTileFragmentPresenter::class.java.simpleName)
                }

                if (newDashboardTileFragmentPresenter != null) {
                    dashboard.bottomLeftTileImplementationTypeName = newDashboardTileFragmentPresenter::class.java.simpleName
                }
            }
            "bottomRight" -> { // Altitude
                when (currentDashbordTileFragmentPresenter::class.java) {
                    CurrentAltitudeDashboardTileFragmentPresenter::class.java ->
                        newDashboardTileFragmentPresenter = this.dashboardTileFragmentPresenterFactory.createPresenterFromTypeName(AverageAltitudeDashboardTileFragmentPresenter::class.java.simpleName)
                    AverageAltitudeDashboardTileFragmentPresenter::class.java ->
                        newDashboardTileFragmentPresenter = this.dashboardTileFragmentPresenterFactory.createPresenterFromTypeName(MaximumAltitudeDashboardTileFragmentPresenter::class.java.simpleName)
                    MaximumAltitudeDashboardTileFragmentPresenter::class.java ->
                        newDashboardTileFragmentPresenter = this.dashboardTileFragmentPresenterFactory.createPresenterFromTypeName(MinimumAltitudeDashboardTileFragmentPresenter::class.java.simpleName)
                    MinimumAltitudeDashboardTileFragmentPresenter::class.java ->
                        newDashboardTileFragmentPresenter = this.dashboardTileFragmentPresenterFactory.createPresenterFromTypeName(CurrentAltitudeDashboardTileFragmentPresenter::class.java.simpleName)
                }

                if (newDashboardTileFragmentPresenter != null) {
                    dashboard.bottomRightTileImplementationTypeName = newDashboardTileFragmentPresenter::class.java.simpleName
                }
            }
            else -> {
                newDashboardTileFragmentPresenter = null
            }
        }

        if (newDashboardTileFragmentPresenter != null) {
            this.dashboardService.saveDashboard(dashboard)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { _ ->
                        dashboardTileFragment.presenter = newDashboardTileFragmentPresenter

                        val toastText = this.view.getString(R.string.dashboard_tile_changed_toast_text, newDashboardTileFragmentPresenter.title)
                        Toast.makeText(this.view.context, toastText, Toast.LENGTH_SHORT).show()
                    }
        }
    }

    public fun setUserVisibleHint(isVisibleToUser: Boolean) {
        if(this.view.activity is INeedFragmentVisibilityInfo) {
            (this.view.activity as INeedFragmentVisibilityInfo).onFragmentVisibilityChange(this.view, isVisibleToUser)
        }
    }
}