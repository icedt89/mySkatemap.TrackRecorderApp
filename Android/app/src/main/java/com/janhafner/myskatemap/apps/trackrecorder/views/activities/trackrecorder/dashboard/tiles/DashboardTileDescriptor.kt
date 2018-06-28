package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles

import android.support.annotation.DrawableRes
import android.support.annotation.LayoutRes
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.altitude.AverageAltitudeDashboardTileFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.altitude.CurrentAltitudeDashboardTileFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.altitude.MaximumAltitudeDashboardTileFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.altitude.MinimumAltitudeDashboardTileFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.ambienttemperature.AverageAmbientTemperatureDashboardTileFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.ambienttemperature.CurrentAmbientTemperatureDashboardTileFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.ambienttemperature.MaximumAmbientTemperatureDashboardTileFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.ambienttemperature.MinimumAmbientTemperatureDashboardTileFragment
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
            result.add(DashboardTileDescriptor("AverageAltitudeDashboardTileFragment", AverageAltitudeDashboardTileFragment::javaClass.name))
            result.add(DashboardTileDescriptor("CurrentAltitudeDashboardTileFragment", CurrentAltitudeDashboardTileFragment::javaClass.name))
            result.add(DashboardTileDescriptor("MaximumAltitudeDashboardTileFragment", MaximumAltitudeDashboardTileFragment::javaClass.name))
            result.add(DashboardTileDescriptor("MinimumAltitudeDashboardTileFragment", MinimumAltitudeDashboardTileFragment::javaClass.name))
            result.add(DashboardTileDescriptor("AverageAmbientTemperatureDashboardTileFragment", AverageAmbientTemperatureDashboardTileFragment::javaClass.name))
            result.add(DashboardTileDescriptor("CurrentAmbientTemperatureDashboardTileFragment", CurrentAmbientTemperatureDashboardTileFragment::javaClass.name))
            result.add(DashboardTileDescriptor("MaximumAmbientTemperatureDashboardTileFragment", MaximumAmbientTemperatureDashboardTileFragment::javaClass.name))
            result.add(DashboardTileDescriptor("MinimumAmbientTemperatureDashboardTileFragment", MinimumAmbientTemperatureDashboardTileFragment::javaClass.name))
            result.add(DashboardTileDescriptor("BurnedEnergyDashboardTileFragment", BurnedEnergyDashboardTileFragment::javaClass.name))
            result.add(DashboardTileDescriptor("DistanceDashboardTileFragment", DistanceDashboardTileFragment::javaClass.name))
            result.add(DashboardTileDescriptor("AverageSpeedDashboardTileFragment", AverageSpeedDashboardTileFragment::javaClass.name))
            result.add(DashboardTileDescriptor("CurrentSpeedDashboardTileFragment", CurrentSpeedDashboardTileFragment::javaClass.name))
            result.add(DashboardTileDescriptor("MaximumSpeedDashboardTileFragment", MaximumSpeedDashboardTileFragment::javaClass.name))

            result
        }

        public fun getDescriptors() : List<DashboardTileDescriptor> {
            return lazyDescriptors.value
        }
    }
}