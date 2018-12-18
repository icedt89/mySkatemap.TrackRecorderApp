package com.janhafner.myskatemap.apps.trackrecorder

import com.janhafner.myskatemap.apps.trackrecorder.modules.ApplicationModule
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderService
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.about.AboutActivity
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.appsettings.AppSettingsFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.playground.PlaygroundActivity
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.tracklist.TrackListActivity
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.TrackRecorderActivity
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.DashboardTabFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.map.MapTabFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.userprofilesettings.UserProfileSettingsFragment
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ApplicationModule::class])
internal interface ApplicationComponent : DashboardTileFragmentPresenterComponent {
    fun inject(trackRecorderApplication: TrackRecorderApplication)

    fun inject(playgroundActivity: PlaygroundActivity)

    fun inject(aboutActivity: AboutActivity)

    fun inject(trackRecorderActivity: TrackRecorderActivity)

    fun inject(trackListActivity: TrackListActivity)

    fun inject(dashboardTabFragment: DashboardTabFragment)

    fun inject(appSettingsFragment: AppSettingsFragment)

    fun inject(userProfileSettingsFragment: UserProfileSettingsFragment)

    fun inject(trackRecorderService: TrackRecorderService)

    fun inject(mapTabFragment: MapTabFragment)
}