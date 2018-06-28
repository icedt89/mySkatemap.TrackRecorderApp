package com.janhafner.myskatemap.apps.trackrecorder

import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderService
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.settings.SettingsFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.userprofile.settings.UserProfileSettingsFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.tracklist.TrackListActivity
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.TrackRecorderActivity
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.DashboardTabFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.DashboardTileFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.data.DataTabFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.map.MapTabFragment
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ApplicationModule::class])
internal interface ApplicationComponent {
    fun inject(trackRecorderActivity: TrackRecorderActivity)

    fun inject(trackListActivity: TrackListActivity)

    fun inject(mapTabFragment: MapTabFragment)

    fun inject(dataTabFragment: DataTabFragment)

    fun inject(dashboardTabFragment: DashboardTabFragment)

    fun inject(settingsFragment: SettingsFragment)

    fun inject(userProfileSettingsFragment: UserProfileSettingsFragment)

    fun inject(trackRecorderService: TrackRecorderService)

    fun inject(dashboardTileFragment: DashboardTileFragment)
}