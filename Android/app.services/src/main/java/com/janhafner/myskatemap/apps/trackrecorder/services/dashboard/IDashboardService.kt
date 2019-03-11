package com.janhafner.myskatemap.apps.trackrecorder.services.dashboard

import com.janhafner.myskatemap.apps.trackrecorder.core.types.Dashboard
import io.reactivex.Single

public interface IDashboardService {
    fun getDashboard() : Single<Dashboard>

    fun saveDashboard(dashboard: Dashboard): Single<Unit>
}