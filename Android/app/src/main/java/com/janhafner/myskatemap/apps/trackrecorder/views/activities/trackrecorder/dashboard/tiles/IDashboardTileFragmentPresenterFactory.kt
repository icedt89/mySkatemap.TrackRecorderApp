package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles

internal interface IDashboardTileFragmentPresenterFactory {
    fun createPresenterFromTypeName(typeName: String) : DashboardTileFragmentPresenter
}