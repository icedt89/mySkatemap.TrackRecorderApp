package com.janhafner.myskatemap.apps.trackrecorder.services.dashboard

import com.janhafner.myskatemap.apps.trackrecorder.common.Optional
import com.janhafner.myskatemap.apps.trackrecorder.common.types.Dashboard
import io.reactivex.Single

public final class ReturnAlwaysDefaultDashboardDataSource : IDashboardDataSource {
    private val defaultDashboard: Dashboard = Dashboard()

    override fun getDashboardByIdOrNull(id: String): Single<Optional<Dashboard>> {
        return Single.just(Optional(this.defaultDashboard))
    }
}