package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles

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
            AverageAmbientTemperatureDashboardTileFragment::class.java.name -> {
                return AverageAmbientTemperatureDashboardTileFragment(layoutId)
            }
            CurrentAmbientTemperatureDashboardTileFragment::class.java.name -> {
                return CurrentAmbientTemperatureDashboardTileFragment(layoutId)
            }
            MaximumAmbientTemperatureDashboardTileFragment::class.java.name -> {
                return MaximumAmbientTemperatureDashboardTileFragment(layoutId)
            }
            MinimumAmbientTemperatureDashboardTileFragment::class.java.name -> {
                return MinimumAmbientTemperatureDashboardTileFragment(layoutId)
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