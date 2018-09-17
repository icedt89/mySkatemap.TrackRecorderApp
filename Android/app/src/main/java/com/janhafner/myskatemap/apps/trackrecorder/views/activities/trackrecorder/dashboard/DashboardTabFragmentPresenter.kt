package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard

import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.findChildFragmentById
import com.janhafner.myskatemap.apps.trackrecorder.services.dashboard.IDashboardService
import com.janhafner.myskatemap.apps.trackrecorder.views.INeedFragmentVisibilityInfo
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.DashboardTileFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.DashboardTileFragmentPresenter
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.IDashboardTileFragmentPresenterFactory
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

internal final class DashboardTabFragmentPresenter(private val view: DashboardTabFragment,
                                                   private val dashboardService: IDashboardService,
                                                   private val dashboardTileFragmentPresenterFactory: IDashboardTileFragmentPresenterFactory) {
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
                    this.view.findChildFragmentById<DashboardTileFragment>(R.id.trackrecorderactivity_tab_dashboard_tile_top_left)
                            .setPresenter(dashboardResult.topLeftFragmentPresenter)
                    this.view.findChildFragmentById<DashboardTileFragment>(R.id.trackrecorderactivity_tab_dashboard_tile_top_right)
                            .setPresenter(dashboardResult.topRightFragmentPresenter)
                    this.view.findChildFragmentById<DashboardTileFragment>(R.id.trackrecorderactivity_tab_dashboard_tile_middle_center)
                            .setPresenter(dashboardResult.middleCenterFragmentPresenter)
                    this.view.findChildFragmentById<DashboardTileFragment>(R.id.trackrecorderactivity_tab_dashboard_tile_bottom_left)
                            .setPresenter(dashboardResult.bottomLeftFragmentPresenter)
                    this.view.findChildFragmentById<DashboardTileFragment>(R.id.trackrecorderactivity_tab_dashboard_tile_bottom_right)
                            .setPresenter(dashboardResult.bottomRightFragmentPresenter)
                }
    }

    public fun setUserVisibleHint(isVisibleToUser: Boolean) {
        if(this.view.activity is INeedFragmentVisibilityInfo) {
            (this.view.activity as INeedFragmentVisibilityInfo).onFragmentVisibilityChange(this.view, isVisibleToUser)
        }
    }
}