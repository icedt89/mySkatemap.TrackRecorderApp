package com.janhafner.myskatemap.apps.trackrecorder

import com.janhafner.myskatemap.apps.trackrecorder.modules.ApplicationModule
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderService
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.appsettings.AppSettingsFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.TrackRecorderActivity
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.DashboardTabFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.DashboardTileFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.altitude.AverageAltitudeDashboardTileFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.altitude.CurrentAltitudeDashboardTileFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.altitude.MaximumAltitudeDashboardTileFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.altitude.MinimumAltitudeDashboardTileFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.burnedenergy.BurnedEnergyDashboardTileFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.distance.DistanceDashboardTileFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.speed.AverageSpeedDashboardTileFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.speed.CurrentSpeedDashboardTileFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.speed.MaximumSpeedDashboardTileFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.debug.DebugTabFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.map.MapTabFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.userprofilesettings.UserProfileSettingsFragment
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ApplicationModule::class])
internal interface ApplicationComponent {
    fun inject(trackRecorderActivity: TrackRecorderActivity)

    fun inject(mapTabFragment: MapTabFragment)

    fun inject(debugTabFragment: DebugTabFragment)

    fun inject(dashboardTabFragment: DashboardTabFragment)

    fun inject(appSettingsFragment: AppSettingsFragment)

    fun inject(userProfileSettingsFragment: UserProfileSettingsFragment)

    fun inject(trackRecorderService: TrackRecorderService)

    fun inject(dashboardTileFragment: DashboardTileFragment)

    fun inject(dashboardTileFragment: BurnedEnergyDashboardTileFragment)

    fun inject(dashboardTileFragment: DistanceDashboardTileFragment)

    fun inject(dashboardTileFragment: AverageAltitudeDashboardTileFragment)

    fun inject(dashboardTileFragment: CurrentAltitudeDashboardTileFragment)

    fun inject(dashboardTileFragment: MinimumAltitudeDashboardTileFragment)

    fun inject(dashboardTileFragment: MaximumAltitudeDashboardTileFragment)

    fun inject(dashboardTileFragment: AverageSpeedDashboardTileFragment)

    fun inject(dashboardTileFragment: CurrentSpeedDashboardTileFragment)

    fun inject(dashboardTileFragment: MaximumSpeedDashboardTileFragment)
}
