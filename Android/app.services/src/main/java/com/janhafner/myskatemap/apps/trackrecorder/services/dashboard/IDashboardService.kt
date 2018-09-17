package com.janhafner.myskatemap.apps.trackrecorder.services.dashboard

import com.janhafner.myskatemap.apps.trackrecorder.services.models.Dashboard
import io.reactivex.Single

public interface IDashboardService {
    fun getCurrentDashboardOrDefault() : Single<Dashboard>
}