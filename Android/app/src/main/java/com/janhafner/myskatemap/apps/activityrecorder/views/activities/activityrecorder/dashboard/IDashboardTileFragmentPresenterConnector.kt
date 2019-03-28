package com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.dashboard

import com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.dashboard.tiles.DashboardTileFragment
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.dashboard.tiles.FormattedDisplayValue
import io.reactivex.Observable
import io.reactivex.disposables.Disposable

internal interface IDashboardTileFragmentPresenterConnector {
    fun connect(dashboardTileFragment: DashboardTileFragment, source: Observable<FormattedDisplayValue>): List<Disposable>
}

