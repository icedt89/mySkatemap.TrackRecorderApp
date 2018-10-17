package com.janhafner.myskatemap.apps.trackrecorder.services.dashboard

import com.janhafner.myskatemap.apps.trackrecorder.common.Optional
import com.janhafner.myskatemap.apps.trackrecorder.common.types.Dashboard
import io.reactivex.Single

public interface IDashboardDataSource {
    fun getDashboardByIdOrNull(id: String) : Single<Optional<Dashboard>>
}

