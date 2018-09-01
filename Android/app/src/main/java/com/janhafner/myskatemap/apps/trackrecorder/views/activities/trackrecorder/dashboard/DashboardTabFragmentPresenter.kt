package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard

import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.getByIdOrDefault
import com.janhafner.myskatemap.apps.trackrecorder.services.ICrudRepository
import com.janhafner.myskatemap.apps.trackrecorder.services.dashboard.Dashboard
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.views.INeedFragmentVisibilityInfo
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.IDashboardTileFragmentFactory
import io.reactivex.disposables.CompositeDisposable

internal final class DashboardTabFragmentPresenter(private val view: DashboardTabFragment,
                                                   private val appSettings: IAppSettings,
                                                   private val dashboardService: ICrudRepository<Dashboard>,
                                                   private val dashboardTileFragmentFactory: IDashboardTileFragmentFactory) {
    private val subscriptions: CompositeDisposable = CompositeDisposable()

    init {
        // The dashboard is either the one from the app app_settings or a default one.
        // Because it does'nt matter we save it always!
        val allDashboards = this.dashboardService.getAll()
        val ooo = allDashboards

        val dashboard = this.dashboardService.getByIdOrDefault(this.appSettings.currentDashboardId)
        this.dashboardService.save(dashboard)
        this.appSettings.currentDashboardId = dashboard.id

        val topLeftFragment = this.dashboardTileFragmentFactory.createInstance(dashboard.topLeftTileImplementationTypeName, R.layout.fragment_dashboard_tile_default)
        val topRightFragment = this.dashboardTileFragmentFactory.createInstance(dashboard.topRightTileImplementationTypeName, R.layout.fragment_dashboard_tile_default)
        val middleCenterFragment = this.dashboardTileFragmentFactory.createInstance(dashboard.middleCenterTileImplementationTypeName, R.layout.fragment_dashboard_tile_main)
        val bottomLeftFragment = this.dashboardTileFragmentFactory.createInstance(dashboard.bottomLeftTileImplementationTypeName, R.layout.fragment_dashboard_tile_default)
        val bottomRightFragment = this.dashboardTileFragmentFactory.createInstance(dashboard.bottomRightTileImplementationTypeName, R.layout.fragment_dashboard_tile_default)

        /*
        this.subscriptions.addAll(
                this.view.trackrecorderactivity_tab_dashboard_tile_top_left.longClicks().subscribe {
                    val tiles = DashboardTileDescriptor.getDescriptors()

                    val gl = GridLayout(this.view.context!!)
                    gl.columnCount = 1
                    gl.rowCount = tiles.count()

                    for ((index, tile) in tiles.withIndex()) {
                        val v = Button(this.view.context!!)
                        v.text = tile.tileSelectorTitle


                        val params = GridLayout.LayoutParams()
                        params.rowSpec = GridLayout.spec(index, 1)
                        params.columnSpec = GridLayout.spec(0, 1)

                        v.layoutParams = params


                        gl.addView(v)
                    }

                    val b =AlertDialog.Builder(this.view.context!!).setView(gl)
b.show()
//                    val popupMenu = this.createPopupMenu(this.view.trackrecorderactivity_tab_dashboard_tile_top_left)
  //                  popupMenu.show()
                },

                this.view.trackrecorderactivity_tab_dashboard_tile_top_right.longClicks().subscribe {

                },

                this.view.trackrecorderactivity_tab_dashboard_tile_middle_center.longClicks().subscribe {

                },

                this.view.trackrecorderactivity_tab_dashboard_tile_bottom_left.longClicks().subscribe {

                },

                this.view.trackrecorderactivity_tab_dashboard_tile_bottom_right.longClicks().subscribe {

                }
        )
        */

        this.view.activity!!.supportFragmentManager.beginTransaction()
                .replace(R.id.trackrecorderactivity_tab_dashboard_tile_top_left, topLeftFragment)
                .replace(R.id.trackrecorderactivity_tab_dashboard_tile_top_right, topRightFragment)
                .replace(R.id.trackrecorderactivity_tab_dashboard_tile_middle_center, middleCenterFragment)
                .replace(R.id.trackrecorderactivity_tab_dashboard_tile_bottom_left, bottomLeftFragment)
                .replace(R.id.trackrecorderactivity_tab_dashboard_tile_bottom_right, bottomRightFragment)
                .commit()
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