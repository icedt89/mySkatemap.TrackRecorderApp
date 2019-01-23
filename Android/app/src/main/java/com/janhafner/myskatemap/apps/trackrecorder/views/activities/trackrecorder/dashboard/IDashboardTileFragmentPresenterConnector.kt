package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard

import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.DashboardTileFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.FormattedDisplayValue
import io.reactivex.Observable
import io.reactivex.disposables.Disposable

internal interface IDashboardTileFragmentPresenterConnector {
    fun connect(dashboardTileFragment: DashboardTileFragment, source: Observable<FormattedDisplayValue>): List<Disposable>
}

