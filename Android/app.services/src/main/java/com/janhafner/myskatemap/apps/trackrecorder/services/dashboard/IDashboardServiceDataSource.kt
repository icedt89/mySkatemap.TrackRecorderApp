package com.janhafner.myskatemap.apps.trackrecorder.services.dashboard

import com.janhafner.myskatemap.apps.trackrecorder.core.Optional
import com.janhafner.myskatemap.apps.trackrecorder.core.types.Dashboard
import io.reactivex.Single

public interface IDashboardServiceDataSource {
    fun getDashboardByIdOrNull(id: String) : Single<Optional<Dashboard>>

    fun saveDashboard(dashboard: Dashboard): Single<String>
}

