package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard

import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.getByIdOrDefaultAsync
import com.janhafner.myskatemap.apps.trackrecorder.services.ICrudRepository
import com.janhafner.myskatemap.apps.trackrecorder.services.models.Dashboard
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.views.INeedFragmentVisibilityInfo
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.DashboardTileFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.IDashboardTileFragmentFactory
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

internal final class DashboardTabFragmentPresenter(private val view: DashboardTabFragment,
                                                   private val appSettings: IAppSettings,
                                                   private val dashboardService: ICrudRepository<Dashboard>,
                                                   private val dashboardTileFragmentFactory: IDashboardTileFragmentFactory) {
    private val subscriptions: CompositeDisposable = CompositeDisposable()

    init {
        this.dashboardService.getByIdOrDefaultAsync(this.appSettings.currentDashboardId)
                .subscribeOn(Schedulers.io())
                .map {
                    // No need to .saveAsync(...), we are already async!
                    this.dashboardService.save(it)
                    this.appSettings.currentDashboardId = it.id

                    it
                }
                .observeOn(Schedulers.computation())
                .map {
                    val topLeftFragment = this.dashboardTileFragmentFactory.createInstance(it.topLeftTileImplementationTypeName, R.layout.fragment_dashboard_tile_default)
                    val topRightFragment = this.dashboardTileFragmentFactory.createInstance(it.topRightTileImplementationTypeName, R.layout.fragment_dashboard_tile_default)
                    val middleCenterFragment = this.dashboardTileFragmentFactory.createInstance(it.middleCenterTileImplementationTypeName, R.layout.fragment_dashboard_tile_main)
                    val bottomLeftFragment = this.dashboardTileFragmentFactory.createInstance(it.bottomLeftTileImplementationTypeName, R.layout.fragment_dashboard_tile_default)
                    val bottomRightFragment = this.dashboardTileFragmentFactory.createInstance(it.bottomRightTileImplementationTypeName, R.layout.fragment_dashboard_tile_default)

                    object : Any() {
                        public val topLeftFragment: DashboardTileFragment = topLeftFragment

                        public val topRightFragment: DashboardTileFragment = topRightFragment

                        public val middleCenterFragment: DashboardTileFragment = middleCenterFragment

                        public val bottomLeftFragment: DashboardTileFragment = bottomLeftFragment

                        public val bottomRightFragment: DashboardTileFragment = bottomRightFragment

                        public val dashboard: Dashboard = it
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    dashboardResult, _ ->
                    this.view.activity!!.supportFragmentManager.beginTransaction()
                            .replace(R.id.trackrecorderactivity_tab_dashboard_tile_top_left, dashboardResult.topLeftFragment)
                            .replace(R.id.trackrecorderactivity_tab_dashboard_tile_top_right, dashboardResult.topRightFragment)
                            .replace(R.id.trackrecorderactivity_tab_dashboard_tile_middle_center, dashboardResult.middleCenterFragment)
                            .replace(R.id.trackrecorderactivity_tab_dashboard_tile_bottom_left, dashboardResult.bottomLeftFragment)
                            .replace(R.id.trackrecorderactivity_tab_dashboard_tile_bottom_right, dashboardResult.bottomRightFragment)
                            .commit()
                }
    }

    public fun setUserVisibleHint(isVisibleToUser: Boolean) {
        if(this.view.activity is INeedFragmentVisibilityInfo) {
            (this.view.activity as INeedFragmentVisibilityInfo).onFragmentVisibilityChange(this.view, isVisibleToUser)
        }
    }

    public fun destroy() {
        this.subscriptions.dispose()
    }
}