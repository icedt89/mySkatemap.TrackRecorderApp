package com.janhafner.myskatemap.apps.trackrecorder.services.dashboard

import com.janhafner.myskatemap.apps.trackrecorder.common.types.Dashboard
import io.reactivex.Single

public interface IDashboardService {
    fun getCurrentDashboardOrDefault() : Single<Dashboard>
}