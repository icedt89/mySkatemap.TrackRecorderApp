package com.janhafner.myskatemap.apps.trackrecorder.services.dashboard

import com.janhafner.myskatemap.apps.trackrecorder.services.models.Dashboard
import io.reactivex.Single
import java.util.*

public final class DashboardService(private val localDashboardDataSource: IDashboardDataSource) : IDashboardService {
    public override fun getCurrentDashboardOrDefault() : Single<Dashboard> {
        // TODO: get from app settings
        val currentDashboardId = UUID.randomUUID().toString()

        return this.localDashboardDataSource.getDashboardByIdOrNull(currentDashboardId)
                .map {
                    if (it.value == null) {
                        Dashboard()
                    } else {
                        it.value
                    }
                }
    }
}