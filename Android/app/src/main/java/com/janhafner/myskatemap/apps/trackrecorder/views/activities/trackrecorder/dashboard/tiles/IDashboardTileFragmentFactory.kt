package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles

import android.support.annotation.LayoutRes

internal interface IDashboardTileFragmentFactory {
    fun createInstance(className: String, @LayoutRes layoutId: Int) : DashboardTileFragment
}