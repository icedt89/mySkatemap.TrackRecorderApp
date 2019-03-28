package com.janhafner.myskatemap.apps.activityrecorder.modules

import android.content.Context
import com.janhafner.myskatemap.apps.activityrecorder.conversion.distance.IDistanceConverterFactory
import com.janhafner.myskatemap.apps.activityrecorder.conversion.energy.IEnergyConverterFactory
import com.janhafner.myskatemap.apps.activityrecorder.conversion.speed.ISpeedConverterFactory
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.IServiceController
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.ActivityRecorderServiceBinder
import com.janhafner.myskatemap.apps.activityrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.dashboard.tiles.BurnedEnergyDashboardTileFragmentPresenter
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.dashboard.tiles.DistanceDashboardTileFragmentPresenter
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.dashboard.tiles.NumberOfLocationsDashboardTileFragmentPresenter
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.dashboard.tiles.RecordingTimeDashboardTileFragmentPresenter
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.dashboard.tiles.altitude.AverageAltitudeDashboardTileFragmentPresenter
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.dashboard.tiles.altitude.CurrentAltitudeDashboardTileFragmentPresenter
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.dashboard.tiles.altitude.MaximumAltitudeDashboardTileFragmentPresenter
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.dashboard.tiles.altitude.MinimumAltitudeDashboardTileFragmentPresenter
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.dashboard.tiles.speed.AverageSpeedDashboardTileFragmentPresenter
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.dashboard.tiles.speed.CurrentSpeedDashboardTileFragmentPresenter
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.dashboard.tiles.speed.MaximumSpeedDashboardTileFragmentPresenter
import dagger.Module
import dagger.Provides

@Module
internal final class DashboardTileFragmentPresenterModule {
    @Provides
    public fun provideAverageAltitudeDashboardTileFragmentPresenter(context: Context,
                                                                    appSettings: IAppSettings,
                                                                    activityRecorderServiceController: IServiceController<ActivityRecorderServiceBinder>,
                                                                    distanceConverterFactory: IDistanceConverterFactory): AverageAltitudeDashboardTileFragmentPresenter {
        return AverageAltitudeDashboardTileFragmentPresenter(context, appSettings, activityRecorderServiceController, distanceConverterFactory)
    }

    @Provides
    public fun provideRecordingTimeDashboardTileFragmentPresenter(context: Context,
                                                                  activityRecorderServiceController: IServiceController<ActivityRecorderServiceBinder>): RecordingTimeDashboardTileFragmentPresenter {
        return RecordingTimeDashboardTileFragmentPresenter(context, activityRecorderServiceController)
    }

    @Provides
    public fun provideCurrentAltitudeDashboardTileFragmentPresenter(context: Context,
                                                                    appSettings: IAppSettings,
                                                                    activityRecorderServiceController: IServiceController<ActivityRecorderServiceBinder>,
                                                                    distanceConverterFactory: IDistanceConverterFactory): CurrentAltitudeDashboardTileFragmentPresenter {
        return CurrentAltitudeDashboardTileFragmentPresenter(context, appSettings, activityRecorderServiceController, distanceConverterFactory)
    }

    @Provides
    public fun provideMinimumAltitudeDashboardTileFragmentPresenter(context: Context,
                                                                    appSettings: IAppSettings,
                                                                    activityRecorderServiceController: IServiceController<ActivityRecorderServiceBinder>,
                                                                    distanceConverterFactory: IDistanceConverterFactory): MinimumAltitudeDashboardTileFragmentPresenter {
        return MinimumAltitudeDashboardTileFragmentPresenter(context, appSettings, activityRecorderServiceController, distanceConverterFactory)
    }

    @Provides
    public fun provideMaximumAltitudeDashboardTileFragmentPresenter(context: Context,
                                                                    appSettings: IAppSettings,
                                                                    activityRecorderServiceController: IServiceController<ActivityRecorderServiceBinder>,
                                                                    distanceConverterFactory: IDistanceConverterFactory): MaximumAltitudeDashboardTileFragmentPresenter {
        return MaximumAltitudeDashboardTileFragmentPresenter(context, appSettings, activityRecorderServiceController, distanceConverterFactory)
    }

    @Provides
    public fun provideNumberOfLocationsDashboardTileFragmentPresenter(context: Context, activityRecorderServiceController: IServiceController<ActivityRecorderServiceBinder>): NumberOfLocationsDashboardTileFragmentPresenter {
        return NumberOfLocationsDashboardTileFragmentPresenter(context, activityRecorderServiceController)
    }

    @Provides
    public fun provideDistanceDashboardTileFragmentPresenter(context: Context,
                                                             appSettings: IAppSettings,
                                                             activityRecorderServiceController: IServiceController<ActivityRecorderServiceBinder>,
                                                             distanceConverterFactory: IDistanceConverterFactory): DistanceDashboardTileFragmentPresenter {
        return DistanceDashboardTileFragmentPresenter(context, appSettings, activityRecorderServiceController, distanceConverterFactory)
    }

    @Provides
    public fun provideBurnedEnergyDashboardTileFragmentPresenter(context: Context,
                                                                 appSettings: IAppSettings,
                                                                 activityRecorderServiceController: IServiceController<ActivityRecorderServiceBinder>,
                                                                 energyConverterFactory: IEnergyConverterFactory): BurnedEnergyDashboardTileFragmentPresenter {
        return BurnedEnergyDashboardTileFragmentPresenter(context, appSettings, activityRecorderServiceController, energyConverterFactory)
    }

    @Provides
    public fun provideAverageSpeedDashboardTileFragmentPresenter(context: Context,
                                                                 appSettings: IAppSettings,
                                                                 activityRecorderServiceController: IServiceController<ActivityRecorderServiceBinder>,
                                                                 speedConverterFactory: ISpeedConverterFactory): AverageSpeedDashboardTileFragmentPresenter {
        return AverageSpeedDashboardTileFragmentPresenter(context, appSettings, activityRecorderServiceController, speedConverterFactory)
    }

    @Provides
    public fun provideCurrentSpeedDashboardTileFragmentPresenter(context: Context,
                                                                 appSettings: IAppSettings,
                                                                 activityRecorderServiceController: IServiceController<ActivityRecorderServiceBinder>,
                                                                 speedConverterFactory: ISpeedConverterFactory): CurrentSpeedDashboardTileFragmentPresenter {
        return CurrentSpeedDashboardTileFragmentPresenter(context, appSettings, activityRecorderServiceController, speedConverterFactory)
    }

    @Provides
    public fun provideMaximumSpeedDashboardTileFragmentPresenter(context: Context,
                                                                 appSettings: IAppSettings,
                                                                 activityRecorderServiceController: IServiceController<ActivityRecorderServiceBinder>,
                                                                 speedConverterFactory: ISpeedConverterFactory): MaximumSpeedDashboardTileFragmentPresenter {
        return MaximumSpeedDashboardTileFragmentPresenter(context, appSettings, activityRecorderServiceController, speedConverterFactory)
    }
}

