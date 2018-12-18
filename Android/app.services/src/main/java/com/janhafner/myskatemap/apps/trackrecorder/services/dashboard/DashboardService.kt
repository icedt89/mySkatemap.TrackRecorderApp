package com.janhafner.myskatemap.apps.trackrecorder.services.dashboard

import com.janhafner.myskatemap.apps.trackrecorder.common.types.Dashboard
import io.reactivex.Single
import java.util.*

public final class DashboardService(private val localDashboardServiceDataSource: IDashboardServiceDataSource) : IDashboardService {
    public override fun getCurrentDashboardOrDefault() : Single<Dashboard> {
        // TODO: get from app settings
        val currentDashboardId = UUID.fromString("00000000-0000-0000-0000-000000000000")

        return this.localDashboardServiceDataSource.getDashboardByIdOrNull(currentDashboardId.toString())
                .map {
                    if (it.value == null) {
                        val defaultDashboard = Dashboard(currentDashboardId)

                        defaultDashboard.topLeftTileImplementationTypeName = "DistanceDashboardTileFragmentPresenter"
                        defaultDashboard.topRightTileImplementationTypeName = "BurnedEnergyDashboardTileFragmentPresenter"
                        defaultDashboard.middleCenterTileImplementationTypeName = "RecordingTimeDashboardTileFragmentPresenter"
                        defaultDashboard.bottomLeftTileImplementationTypeName = "AverageSpeedDashboardTileFragmentPresenter"
                        defaultDashboard.bottomRightTileImplementationTypeName = "CurrentAltitudeDashboardTileFragmentPresenter"

                        defaultDashboard
                    } else {
                        it.value
                    }
                }
    }

    public override fun saveDashboard(dashboard: Dashboard): Single<Unit> {
        return this.localDashboardServiceDataSource.saveDashboard(dashboard)
                .map {
                    Unit
                }
    }
}