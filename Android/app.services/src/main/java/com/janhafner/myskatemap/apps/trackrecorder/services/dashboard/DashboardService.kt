package com.janhafner.myskatemap.apps.trackrecorder.services.dashboard

import com.janhafner.myskatemap.apps.trackrecorder.common.types.Dashboard
import io.reactivex.Single
import java.util.*

public final class DashboardService(private val localDashboardServiceDataSource: IDashboardServiceDataSource) : IDashboardService {
    public override fun getDashboard() : Single<Dashboard> {
        val currentDashboardId = UUID.fromString("00000000-0000-0000-0000-000000000000")

        return this.localDashboardServiceDataSource.getDashboardByIdOrNull(currentDashboardId.toString())
                .map {
                    if (it.value == null) {
                        val defaultDashboard = Dashboard(currentDashboardId)

                        defaultDashboard.topLeftTile.implementationTypeName = "DistanceDashboardTileFragmentPresenter"
                        defaultDashboard.topRightTile.implementationTypeName = "BurnedEnergyDashboardTileFragmentPresenter"
                        defaultDashboard.middleCenterTile.implementationTypeName = "RecordingTimeDashboardTileFragmentPresenter"
                        defaultDashboard.bottomLeftTile.implementationTypeName = "CurrentSpeedDashboardTileFragmentPresenter"
                        defaultDashboard.bottomRightTile.implementationTypeName = "CurrentAltitudeDashboardTileFragmentPresenter"

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