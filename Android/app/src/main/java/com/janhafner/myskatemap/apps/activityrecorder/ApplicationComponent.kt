package com.janhafner.myskatemap.apps.activityrecorder

import com.janhafner.myskatemap.apps.activityrecorder.modules.ApplicationModule
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.ActivityRecorderService
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.about.AboutActivity
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.appsettings.AppSettingsFragment
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.playground.PlaygroundActivity
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityhistory.ActivityHistoryActivity
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.ActivityRecorderActivity
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.dashboard.DashboardTabFragment
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.map.MapTabFragment
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.userprofilesettings.UserProfileSettingsFragment
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.viewfinishedactivity.ViewFinishedActivityActivity
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.viewfinishedactivity.overview.OverviewTabFragment
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ApplicationModule::class])
internal interface ApplicationComponent : DashboardTileFragmentPresenterComponent {
    fun inject(trackRecorderApplication: TrackRecorderApplication)

    fun inject(playgroundActivity: PlaygroundActivity)

    fun inject(aboutActivity: AboutActivity)

    fun inject(activityRecorderActivity: ActivityRecorderActivity)

    fun inject(viewFinishedActivityActivity: ViewFinishedActivityActivity)

    fun inject(mapTabFragment: com.janhafner.myskatemap.apps.activityrecorder.views.activities.viewfinishedactivity.map.MapTabFragment)

    fun inject(trackListActivity: ActivityHistoryActivity)

    fun inject(overviewTabFragment: OverviewTabFragment)

    fun inject(dashboardTabFragment: DashboardTabFragment)

    fun inject(appSettingsFragment: AppSettingsFragment)

    fun inject(userProfileSettingsFragment: UserProfileSettingsFragment)

    fun inject(activityRecorderService: ActivityRecorderService)

    fun inject(mapTabFragment: MapTabFragment)
}