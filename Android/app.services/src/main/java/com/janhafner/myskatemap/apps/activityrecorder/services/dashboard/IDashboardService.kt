package com.janhafner.myskatemap.apps.activityrecorder.services.dashboard

import com.janhafner.myskatemap.apps.activityrecorder.core.types.Dashboard
import io.reactivex.Single

public interface IDashboardService {
    fun getDashboard() : Single<Dashboard>

    fun saveDashboard(dashboard: Dashboard): Single<Unit>
}