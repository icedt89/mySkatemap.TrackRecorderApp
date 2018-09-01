package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles

import android.support.annotation.LayoutRes
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.altitude.AverageAltitudeDashboardTileFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.altitude.CurrentAltitudeDashboardTileFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.altitude.MaximumAltitudeDashboardTileFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.altitude.MinimumAltitudeDashboardTileFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.burnedenergy.BurnedEnergyDashboardTileFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.distance.DistanceDashboardTileFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.speed.AverageSpeedDashboardTileFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.speed.CurrentSpeedDashboardTileFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.speed.MaximumSpeedDashboardTileFragment

internal final class DashboardTileFragmentFactory : IDashboardTileFragmentFactory {
    public override fun createInstance(className: String, @LayoutRes layoutId: Int) : DashboardTileFragment {
        when(className) {
            AverageAltitudeDashboardTileFragment::class.java.name -> {
                return AverageAltitudeDashboardTileFragment(layoutId)
            }
            CurrentAltitudeDashboardTileFragment::class.java.name -> {
                return CurrentAltitudeDashboardTileFragment(layoutId)
            }
            MaximumAltitudeDashboardTileFragment::class.java.name -> {
                return MaximumAltitudeDashboardTileFragment(layoutId)
            }
            MinimumAltitudeDashboardTileFragment::class.java.name -> {
                return MinimumAltitudeDashboardTileFragment(layoutId)
            }
            BurnedEnergyDashboardTileFragment::class.java.name -> {
                return BurnedEnergyDashboardTileFragment(layoutId)
            }
            DistanceDashboardTileFragment::class.java.name -> {
                return DistanceDashboardTileFragment(layoutId)
            }
            AverageSpeedDashboardTileFragment::class.java.name -> {
                return AverageSpeedDashboardTileFragment(layoutId)
            }
            CurrentSpeedDashboardTileFragment::class.java.name -> {
                return CurrentSpeedDashboardTileFragment(layoutId)
            }
            MaximumSpeedDashboardTileFragment::class.java.name -> {
                return MaximumSpeedDashboardTileFragment(layoutId)
            }
            else -> {
                throw NotImplementedError("Implementation for \"${className}\" not found!")
            }
        }
    }
}