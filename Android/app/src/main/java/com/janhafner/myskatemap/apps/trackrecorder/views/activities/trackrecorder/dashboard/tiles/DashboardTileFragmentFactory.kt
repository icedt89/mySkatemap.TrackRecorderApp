package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles

import android.os.Bundle
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
        val result: DashboardTileFragment
        when(className) {
            AverageAltitudeDashboardTileFragment::class.java.simpleName -> {
                result = AverageAltitudeDashboardTileFragment()
            }
            CurrentAltitudeDashboardTileFragment::class.java.simpleName -> {
                result = CurrentAltitudeDashboardTileFragment()
            }
            MaximumAltitudeDashboardTileFragment::class.java.simpleName -> {
                result = MaximumAltitudeDashboardTileFragment()
            }
            MinimumAltitudeDashboardTileFragment::class.java.simpleName -> {
                result = MinimumAltitudeDashboardTileFragment()
            }
            BurnedEnergyDashboardTileFragment::class.java.simpleName -> {
                result = BurnedEnergyDashboardTileFragment()
            }
            DistanceDashboardTileFragment::class.java.simpleName -> {
                result = DistanceDashboardTileFragment()
            }
            AverageSpeedDashboardTileFragment::class.java.simpleName -> {
                result = AverageSpeedDashboardTileFragment()
            }
            CurrentSpeedDashboardTileFragment::class.java.simpleName -> {
                result = CurrentSpeedDashboardTileFragment()
            }
            MaximumSpeedDashboardTileFragment::class.java.simpleName -> {
                result = MaximumSpeedDashboardTileFragment()
            }
            else -> {
                throw NotImplementedError("Implementation for \"${className}\" not found!")
            }
        }

        val fragmentArguments = Bundle()
        fragmentArguments.putInt(DashboardTileFragment.EXTRAS_LAYOUT_ARGUMENT_KEY, layoutId)

        result.arguments = fragmentArguments

        return result
    }
}