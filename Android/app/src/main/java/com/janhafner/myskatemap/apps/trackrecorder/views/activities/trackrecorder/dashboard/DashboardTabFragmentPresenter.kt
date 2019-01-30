package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard

import android.widget.Toast
import com.jakewharton.rxbinding2.view.longClicks
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.common.ToastManager
import com.janhafner.myskatemap.apps.trackrecorder.common.types.Dashboard
import com.janhafner.myskatemap.apps.trackrecorder.common.types.DashboardTile
import com.janhafner.myskatemap.apps.trackrecorder.common.types.DashboardTileDisplayType
import com.janhafner.myskatemap.apps.trackrecorder.findChildFragmentById
import com.janhafner.myskatemap.apps.trackrecorder.services.dashboard.IDashboardService
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.*
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.altitude.AverageAltitudeDashboardTileFragmentPresenter
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.altitude.CurrentAltitudeDashboardTileFragmentPresenter
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.altitude.MaximumAltitudeDashboardTileFragmentPresenter
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.altitude.MinimumAltitudeDashboardTileFragmentPresenter
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.speed.AverageSpeedDashboardTileFragmentPresenter
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.speed.CurrentSpeedDashboardTileFragmentPresenter
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.speed.MaximumSpeedDashboardTileFragmentPresenter
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_dashboard_tile_default.view.*


internal final class DashboardTabFragmentPresenter(private val view: DashboardTabFragment,
                                                   private val dashboardService: IDashboardService,
                                                   private val dashboardTileFragmentPresenterFactory: IDashboardTileFragmentPresenterFactory) {
    private val subscriptions: CompositeDisposable = CompositeDisposable()

    init {
        this.initialize()
    }

    private fun changeTileFragmentPresenter(dashboard: Dashboard, dashboardTileSetup: DashboardTileSetup, position: DashboardTileFragmentPosition): Single<DashboardTileFragmentPresenter> {
        val currentDashbordTileFragmentPresenter = dashboardTileSetup.fragment.presenter!!

        var newDashboardTileFragmentPresenter: DashboardTileFragmentPresenter? = null
        if (position == DashboardTileFragmentPosition.TopLeft) { // Distance, Number of locations
            if (currentDashbordTileFragmentPresenter::class.java == DistanceDashboardTileFragmentPresenter::class.java) {
                newDashboardTileFragmentPresenter = this.dashboardTileFragmentPresenterFactory.createPresenterFromTypeName(NumberOfLocationsDashboardTileFragmentPresenter::class.java.simpleName)
            } else if (currentDashbordTileFragmentPresenter::class.java == NumberOfLocationsDashboardTileFragmentPresenter::class.java) {
                newDashboardTileFragmentPresenter = this.dashboardTileFragmentPresenterFactory.createPresenterFromTypeName(DistanceDashboardTileFragmentPresenter::class.java.simpleName)
            }
        } else if (position == DashboardTileFragmentPosition.BottomLeft) { // Speed
            if (currentDashbordTileFragmentPresenter::class.java == CurrentSpeedDashboardTileFragmentPresenter::class.java) {
                newDashboardTileFragmentPresenter = this.dashboardTileFragmentPresenterFactory.createPresenterFromTypeName(AverageSpeedDashboardTileFragmentPresenter::class.java.simpleName)
            } else if (currentDashbordTileFragmentPresenter::class.java == AverageSpeedDashboardTileFragmentPresenter::class.java) {
                newDashboardTileFragmentPresenter = this.dashboardTileFragmentPresenterFactory.createPresenterFromTypeName(MaximumSpeedDashboardTileFragmentPresenter::class.java.simpleName)
            } else if (currentDashbordTileFragmentPresenter::class.java == MaximumSpeedDashboardTileFragmentPresenter::class.java) {
                newDashboardTileFragmentPresenter = this.dashboardTileFragmentPresenterFactory.createPresenterFromTypeName(CurrentSpeedDashboardTileFragmentPresenter::class.java.simpleName)
            }
        } else if (position == DashboardTileFragmentPosition.BottomRight) { // Altitude
            if (currentDashbordTileFragmentPresenter::class.java == CurrentAltitudeDashboardTileFragmentPresenter::class.java) {
                newDashboardTileFragmentPresenter = this.dashboardTileFragmentPresenterFactory.createPresenterFromTypeName(AverageAltitudeDashboardTileFragmentPresenter::class.java.simpleName)
            } else if (currentDashbordTileFragmentPresenter::class.java == AverageAltitudeDashboardTileFragmentPresenter::class.java) {
                newDashboardTileFragmentPresenter = this.dashboardTileFragmentPresenterFactory.createPresenterFromTypeName(MaximumAltitudeDashboardTileFragmentPresenter::class.java.simpleName)
            } else if (currentDashbordTileFragmentPresenter::class.java == MaximumAltitudeDashboardTileFragmentPresenter::class.java) {
                newDashboardTileFragmentPresenter = this.dashboardTileFragmentPresenterFactory.createPresenterFromTypeName(MinimumAltitudeDashboardTileFragmentPresenter::class.java.simpleName)
            } else if (currentDashbordTileFragmentPresenter::class.java == MinimumAltitudeDashboardTileFragmentPresenter::class.java) {
                newDashboardTileFragmentPresenter = this.dashboardTileFragmentPresenterFactory.createPresenterFromTypeName(CurrentAltitudeDashboardTileFragmentPresenter::class.java.simpleName)
            }
        }

        if (newDashboardTileFragmentPresenter != null) {
            dashboardTileSetup.dashboardTile.implementationTypeName = newDashboardTileFragmentPresenter::class.java.simpleName
            dashboardTileSetup.dashboardTile.displayType = DashboardTileDisplayType.TextOnly

            return this.dashboardService.saveDashboard(dashboard)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.computation())
                    .doOnError {
                        dashboardTileSetup.dashboardTile.implementationTypeName = currentDashbordTileFragmentPresenter::class.java.simpleName
                    }
                    .map {
                        newDashboardTileFragmentPresenter
                    }
        }

        return Single.never()
    }

    private fun changeTileFragmentPresenterConnector(dashboard: Dashboard, dashboardTileSetup: DashboardTileSetup): Single<DashboardTileDisplayType> {
        val currentDashbordTileFragmentPresenter = dashboardTileSetup.fragment.presenter!!

        var newDashbordTileFragmentPresenterDisplayType: DashboardTileDisplayType? = null
        if (currentDashbordTileFragmentPresenter.displayType == DashboardTileDisplayType.TextOnly && currentDashbordTileFragmentPresenter.supportedPresenterConnectorTypes.contains(DashboardTileDisplayType.LineChart)) {
            newDashbordTileFragmentPresenterDisplayType = DashboardTileDisplayType.LineChart
        } else if (currentDashbordTileFragmentPresenter.displayType == DashboardTileDisplayType.LineChart && currentDashbordTileFragmentPresenter.supportedPresenterConnectorTypes.contains(DashboardTileDisplayType.TextOnly)) {
            newDashbordTileFragmentPresenterDisplayType = DashboardTileDisplayType.TextOnly
        }

        if(newDashbordTileFragmentPresenterDisplayType != null) {
            dashboardTileSetup.dashboardTile.displayType = newDashbordTileFragmentPresenterDisplayType

            return this.dashboardService.saveDashboard(dashboard)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.computation())
                    .doOnError {
                        dashboardTileSetup.dashboardTile.displayType = currentDashbordTileFragmentPresenter.displayType
                    }
                    .map {
                        newDashbordTileFragmentPresenterDisplayType
                    }
        }

        return Single.never()
    }

    private fun initialize() {
        this.dashboardService.getDashboard()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .map {
                    val topLeftDashboardTileSetup = DashboardTileSetup()
                    topLeftDashboardTileSetup.dashboardTile = it.topLeftTile

                    val topRightDashboardTileSetup = DashboardTileSetup()
                    topRightDashboardTileSetup.dashboardTile = it.topRightTile

                    val middleCenterDashboardTileSetup = DashboardTileSetup()
                    middleCenterDashboardTileSetup.dashboardTile = it.middleCenterTile

                    val bottomLeftDashboardTileSetup = DashboardTileSetup()
                    bottomLeftDashboardTileSetup.dashboardTile = it.bottomLeftTile

                    val bottomRightDashboardTileSetup = DashboardTileSetup()
                    bottomRightDashboardTileSetup.dashboardTile = it.bottomRightTile

                    object : Any() {
                        public val dashboard: Dashboard = it

                        public val dashboardTileSetups = mapOf(Pair(DashboardTileFragmentPosition.TopLeft, topLeftDashboardTileSetup),
                                Pair(DashboardTileFragmentPosition.TopRight, topRightDashboardTileSetup),
                                Pair(DashboardTileFragmentPosition.MiddleCenter, middleCenterDashboardTileSetup),
                                Pair(DashboardTileFragmentPosition.BottomLeft, bottomLeftDashboardTileSetup),
                                Pair(DashboardTileFragmentPosition.BottomRight, bottomRightDashboardTileSetup))
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .map {
                    val topLeftSetup = it.dashboardTileSetups[DashboardTileFragmentPosition.TopLeft]!!
                    val topLeftFragmentPresenter = this.dashboardTileFragmentPresenterFactory.createPresenterFromTypeName(topLeftSetup.dashboardTile.implementationTypeName)
                    topLeftSetup.fragment = this.view.findChildFragmentById(R.id.trackrecorderactivity_tab_dashboard_tile_top_left)
                    topLeftSetup.fragment.presenter = topLeftFragmentPresenter

                    val topRightSetup = it.dashboardTileSetups[DashboardTileFragmentPosition.TopRight]!!
                    val topRightFragmentPresenter = this.dashboardTileFragmentPresenterFactory.createPresenterFromTypeName(topRightSetup.dashboardTile.implementationTypeName)
                    topRightSetup.fragment = this.view.findChildFragmentById(R.id.trackrecorderactivity_tab_dashboard_tile_top_right)
                    topRightSetup.fragment.presenter = topRightFragmentPresenter

                    val middleCenterSetup = it.dashboardTileSetups[DashboardTileFragmentPosition.MiddleCenter]!!
                    val middleCenterFragmentPresenter = this.dashboardTileFragmentPresenterFactory.createPresenterFromTypeName(middleCenterSetup.dashboardTile.implementationTypeName)
                    middleCenterSetup.fragment = this.view.findChildFragmentById(R.id.trackrecorderactivity_tab_dashboard_tile_middle_center)
                    middleCenterSetup.fragment.presenter = middleCenterFragmentPresenter

                    val bottomLeftSetup = it.dashboardTileSetups[DashboardTileFragmentPosition.BottomLeft]!!
                    val bottomLeftFragmentPresenter = this.dashboardTileFragmentPresenterFactory.createPresenterFromTypeName(bottomLeftSetup.dashboardTile.implementationTypeName)
                    bottomLeftSetup.fragment = this.view.findChildFragmentById(R.id.trackrecorderactivity_tab_dashboard_tile_bottom_left)
                    bottomLeftSetup.fragment.presenter = bottomLeftFragmentPresenter

                    val bottomRightSetup = it.dashboardTileSetups[DashboardTileFragmentPosition.BottomRight]!!
                    val bottomRightFragmentPresenter = this.dashboardTileFragmentPresenterFactory.createPresenterFromTypeName(bottomRightSetup.dashboardTile.implementationTypeName)
                    bottomRightSetup.fragment = this.view.findChildFragmentById(R.id.trackrecorderactivity_tab_dashboard_tile_bottom_right)
                    bottomRightSetup.fragment.presenter = bottomRightFragmentPresenter

                    it
                }
                .subscribe {
                    result ->
                    for (dashboardTileSetup in result.dashboardTileSetups) {
                        dashboardTileSetup.value.fragment.presenter!!.displayType = dashboardTileSetup.value.dashboardTile.displayType

                        this.subscriptions.addAll(
                                dashboardTileSetup.value.fragment.view!!.fragment_dashboard_tile_title.longClicks()
                                        .flatMapSingle {
                                            this.changeTileFragmentPresenter(result.dashboard, dashboardTileSetup.value, dashboardTileSetup.key)
                                        }
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe {
                                            dashboardTileSetup.value.fragment.presenter = it

                                            val toastText = this.view.getString(R.string.dashboard_tile_changed_toast_text, it.title)
                                            ToastManager.showToast(this.view.context!!, toastText, Toast.LENGTH_SHORT)
                                        },
                                dashboardTileSetup.value.fragment.view!!.fragment_dashboard_tile_value.longClicks()
                                        .mergeWith(dashboardTileSetup.value.fragment.view!!.fragment_dashboard_tile_value.longClicks())
                                        .mergeWith(dashboardTileSetup.value.fragment.view!!.fragment_dashboard_tile_line_chart.longClicks())
                                        .flatMapSingle{
                                            this.changeTileFragmentPresenterConnector(result.dashboard, dashboardTileSetup.value)
                                        }
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe {
                                            dashboardTileSetup.value.fragment.presenter!!.displayType = it
                                        }
                        )
                    }
                }
    }

    public fun destroy() {
        this.subscriptions.dispose()
    }

    private final class DashboardTileSetup {
        public lateinit var fragment: DashboardTileFragment

        public lateinit var dashboardTile: DashboardTile
    }
}