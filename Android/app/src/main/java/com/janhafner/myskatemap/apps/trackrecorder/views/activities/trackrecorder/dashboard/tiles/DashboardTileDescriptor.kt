package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles

import android.support.annotation.DrawableRes
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

internal final class DashboardTileDescriptor(public val tileSelectorTitle: String,
                                             private val fragmentTypeName: String,
                                             @DrawableRes public val tileSelectorIconResourceId: Int? = null) {
    public fun createFragment(@LayoutRes fragmentLayout: Int) : DashboardTileFragment {
        return this.createFragment(fragmentLayout)
    }

    companion object {
        private val lazyDescriptors: Lazy<List<DashboardTileDescriptor>> = lazy {
            val result = ArrayList<DashboardTileDescriptor>()

            // TODO
            result.add(DashboardTileDescriptor("AverageAltitudeDashboardTileFragment", AverageAltitudeDashboardTileFragment::class.java.simpleName))
            result.add(DashboardTileDescriptor("CurrentAltitudeDashboardTileFragment", CurrentAltitudeDashboardTileFragment::class.java.simpleName))
            result.add(DashboardTileDescriptor("MaximumAltitudeDashboardTileFragment", MaximumAltitudeDashboardTileFragment::class.java.simpleName))
            result.add(DashboardTileDescriptor("MinimumAltitudeDashboardTileFragment", MinimumAltitudeDashboardTileFragment::class.java.simpleName))
            result.add(DashboardTileDescriptor("BurnedEnergyDashboardTileFragment", BurnedEnergyDashboardTileFragment::class.java.simpleName))
            result.add(DashboardTileDescriptor("DistanceDashboardTileFragment", DistanceDashboardTileFragment::class.java.simpleName))
            result.add(DashboardTileDescriptor("AverageSpeedDashboardTileFragment", AverageSpeedDashboardTileFragment::class.java.simpleName))
            result.add(DashboardTileDescriptor("CurrentSpeedDashboardTileFragment", CurrentSpeedDashboardTileFragment::class.java.simpleName))
            result.add(DashboardTileDescriptor("MaximumSpeedDashboardTileFragment", MaximumSpeedDashboardTileFragment::class.java.simpleName))

            result
        }

        public fun getDescriptors() : List<DashboardTileDescriptor> {
            return lazyDescriptors.value
        }
    }
}