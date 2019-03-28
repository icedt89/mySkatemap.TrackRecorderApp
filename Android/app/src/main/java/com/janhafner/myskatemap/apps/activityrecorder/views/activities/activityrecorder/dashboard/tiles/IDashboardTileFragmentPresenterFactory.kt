package com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.dashboard.tiles

internal interface IDashboardTileFragmentPresenterFactory {
    fun createPresenterFromTypeName(typeName: String) : DashboardTileFragmentPresenter
}