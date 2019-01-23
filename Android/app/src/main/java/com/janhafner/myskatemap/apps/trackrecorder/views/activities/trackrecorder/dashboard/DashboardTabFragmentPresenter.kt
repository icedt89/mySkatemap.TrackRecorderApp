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
import com.janhafner.myskatemap.apps.trackrecorder.views.INeedFragmentVisibilityInfo
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
        this.dashboardService.getCurrentDashboardOrDefault()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .map {
                    val topLeftFragmentPresenter = this.dashboardTileFragmentPresenterFactory.createPresenterFromTypeName(it.topLeftTile.implementationTypeName)
                    val topLeftDashboardTileSetup = DashboardTileSetup()
                    topLeftDashboardTileSetup.presenter = topLeftFragmentPresenter
                    topLeftDashboardTileSetup.dashboardTile = it.topLeftTile

                    val topRightFragmentPresenter = this.dashboardTileFragmentPresenterFactory.createPresenterFromTypeName(it.topRightTile.implementationTypeName)
                    val topRightDashboardTileSetup = DashboardTileSetup()
                    topRightDashboardTileSetup.presenter = topRightFragmentPresenter
                    topRightDashboardTileSetup.dashboardTile = it.topRightTile

                    val middleCenterFragmentPresenter = this.dashboardTileFragmentPresenterFactory.createPresenterFromTypeName(it.middleCenterTile.implementationTypeName)
                    val middleCenterDashboardTileSetup = DashboardTileSetup()
                    middleCenterDashboardTileSetup.presenter = middleCenterFragmentPresenter
                    middleCenterDashboardTileSetup.dashboardTile = it.middleCenterTile

                    val bottomLeftFragmentPresenter = this.dashboardTileFragmentPresenterFactory.createPresenterFromTypeName(it.bottomLeftTile.implementationTypeName)
                    val bottomLeftDashboardTileSetup = DashboardTileSetup()
                    bottomLeftDashboardTileSetup.presenter = bottomLeftFragmentPresenter
                    bottomLeftDashboardTileSetup.dashboardTile = it.bottomLeftTile

                    val bottomRightFragmentPresenter = this.dashboardTileFragmentPresenterFactory.createPresenterFromTypeName(it.bottomRightTile.implementationTypeName)
                    val bottomRightDashboardTileSetup = DashboardTileSetup()
                    bottomRightDashboardTileSetup.presenter = bottomRightFragmentPresenter
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
                    topLeftSetup.fragment = this.view.findChildFragmentById(R.id.trackrecorderactivity_tab_dashboard_tile_top_left)

                    val topRightSetup = it.dashboardTileSetups[DashboardTileFragmentPosition.TopRight]!!
                    topRightSetup.fragment = this.view.findChildFragmentById(R.id.trackrecorderactivity_tab_dashboard_tile_top_right)

                    val middleCenterSetup = it.dashboardTileSetups[DashboardTileFragmentPosition.MiddleCenter]!!
                    middleCenterSetup.fragment = this.view.findChildFragmentById(R.id.trackrecorderactivity_tab_dashboard_tile_middle_center)

                    val bottomLeftSetup = it.dashboardTileSetups[DashboardTileFragmentPosition.BottomLeft]!!
                    bottomLeftSetup.fragment = this.view.findChildFragmentById(R.id.trackrecorderactivity_tab_dashboard_tile_bottom_left)

                    val bottomRightSetup = it.dashboardTileSetups[DashboardTileFragmentPosition.BottomRight]!!
                    bottomRightSetup.fragment = this.view.findChildFragmentById(R.id.trackrecorderactivity_tab_dashboard_tile_bottom_right)

                    it
                }
                .subscribe {
                    result ->
                    for (dashboardTileSetup in result.dashboardTileSetups) {
                        dashboardTileSetup.value.fragment.presenter = dashboardTileSetup.value.presenter
                        dashboardTileSetup.value.presenter.displayType = dashboardTileSetup.value.dashboardTile.displayType

                        this.subscriptions.addAll(
                                dashboardTileSetup.value.fragment.view!!.fragment_dashboard_tile_title.longClicks()
                                        .flatMapSingle {
                                            this.changeTileFragmentPresenter(result.dashboard, dashboardTileSetup.value, dashboardTileSetup.key)
                                        }
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe {
                                            dashboardTileSetup.value.fragment.presenter = it
                                            dashboardTileSetup.value.fragment.presenter!!.displayType = dashboardTileSetup.value.fragment.presenter!!.displayType

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
                                            dashboardTileSetup.value.presenter.displayType = it
                                        }
                        )
                    }
                }
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

            return this.dashboardService.saveDashboard(dashboard)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.computation())
                    .doOnError {
                        dashboardTileSetup.dashboardTile.implementationTypeName = dashboardTileSetup.presenter::class.java.simpleName
                    }
                    .doOnSuccess {
                        dashboardTileSetup.presenter = newDashboardTileFragmentPresenter
                    }
                    .map {
                        newDashboardTileFragmentPresenter
                    }
        }

        return Single.never()
    }

    private fun changeTileFragmentPresenterConnector(dashboard: Dashboard, dashboardTileSetup: DashboardTileSetup): Single<DashboardTileDisplayType> {
        val dashbordTileFragmentPresenter = dashboardTileSetup.fragment.presenter!!

        var newDisplayType: DashboardTileDisplayType? = null
        if (dashbordTileFragmentPresenter.displayType == DashboardTileDisplayType.TextOnly && dashbordTileFragmentPresenter.supportedPresenterConnectorTypes.contains(DashboardTileDisplayType.LineChart)) {
            newDisplayType = DashboardTileDisplayType.LineChart
        } else if (dashbordTileFragmentPresenter.displayType == DashboardTileDisplayType.LineChart && dashbordTileFragmentPresenter.supportedPresenterConnectorTypes.contains(DashboardTileDisplayType.TextOnly)) {
            newDisplayType = DashboardTileDisplayType.TextOnly
        }

        if(newDisplayType != null) {
            dashboardTileSetup.dashboardTile.displayType = newDisplayType

            return this.dashboardService.saveDashboard(dashboard)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.computation())
                    .doOnError {
                        dashboardTileSetup.dashboardTile.displayType = dashbordTileFragmentPresenter.displayType
                    }
                    .map {
                        dashboardTileSetup.dashboardTile.displayType
                    }
        }

        return Single.never()
    }

    public fun setUserVisibleHint(isVisibleToUser: Boolean) {
        if(this.view.activity is INeedFragmentVisibilityInfo) {
            (this.view.activity as INeedFragmentVisibilityInfo).onFragmentVisibilityChange(this.view, isVisibleToUser)
        }
    }

    private final class DashboardTileSetup {
        public lateinit var fragment: DashboardTileFragment

        public lateinit var presenter: DashboardTileFragmentPresenter

        public lateinit var dashboardTile: DashboardTile
    }
}