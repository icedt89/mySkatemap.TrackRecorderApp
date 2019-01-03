package com.janhafner.myskatemap.apps.trackrecorder.modules

import android.content.Context
import com.janhafner.myskatemap.apps.trackrecorder.conversion.distance.IDistanceConverterFactory
import com.janhafner.myskatemap.apps.trackrecorder.conversion.energy.IEnergyConverterFactory
import com.janhafner.myskatemap.apps.trackrecorder.conversion.speed.ISpeedConverterFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.IServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.BurnedEnergyDashboardTileFragmentPresenter
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.DistanceDashboardTileFragmentPresenter
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.NumberOfLocationsDashboardTileFragmentPresenter
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.RecordingTimeDashboardTileFragmentPresenter
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.altitude.AverageAltitudeDashboardTileFragmentPresenter
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.altitude.CurrentAltitudeDashboardTileFragmentPresenter
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.altitude.MaximumAltitudeDashboardTileFragmentPresenter
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.altitude.MinimumAltitudeDashboardTileFragmentPresenter
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.speed.AverageSpeedDashboardTileFragmentPresenter
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.speed.CurrentSpeedDashboardTileFragmentPresenter
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.speed.MaximumSpeedDashboardTileFragmentPresenter
import dagger.Module
import dagger.Provides

@Module
internal final class DashboardTileFragmentPresenterModule {
    @Provides
    public fun provideAverageAltitudeDashboardTileFragmentPresenter(context: Context,
                                                                    appSettings: IAppSettings,
                                                                    trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>,
                                                                    distanceConverterFactory: IDistanceConverterFactory): AverageAltitudeDashboardTileFragmentPresenter {
        return AverageAltitudeDashboardTileFragmentPresenter(context, appSettings, trackRecorderServiceController, distanceConverterFactory)
    }

    @Provides
    public fun provideRecordingTimeDashboardTileFragmentPresenter(context: Context,
                                                                    trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>): RecordingTimeDashboardTileFragmentPresenter {
        return RecordingTimeDashboardTileFragmentPresenter(context, trackRecorderServiceController)
    }

    @Provides
    public fun provideCurrentAltitudeDashboardTileFragmentPresenter(context: Context,
                                                                    appSettings: IAppSettings,
                                                                    trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>,
                                                                    distanceConverterFactory: IDistanceConverterFactory): CurrentAltitudeDashboardTileFragmentPresenter {
        return CurrentAltitudeDashboardTileFragmentPresenter(context, appSettings, trackRecorderServiceController, distanceConverterFactory)
    }

    @Provides
    public fun provideMinimumAltitudeDashboardTileFragmentPresenter(context: Context,
                                                                    appSettings: IAppSettings,
                                                                    trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>,
                                                                    distanceConverterFactory: IDistanceConverterFactory): MinimumAltitudeDashboardTileFragmentPresenter {
        return MinimumAltitudeDashboardTileFragmentPresenter(context, appSettings, trackRecorderServiceController, distanceConverterFactory)
    }

    @Provides
    public fun provideMaximumAltitudeDashboardTileFragmentPresenter(context: Context,
                                                                    appSettings: IAppSettings,
                                                                    trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>,
                                                                    distanceConverterFactory: IDistanceConverterFactory): MaximumAltitudeDashboardTileFragmentPresenter {
        return MaximumAltitudeDashboardTileFragmentPresenter(context, appSettings, trackRecorderServiceController, distanceConverterFactory)
    }

    @Provides
    public fun provideNumberOfLocationsDashboardTileFragmentPresenter(context: Context, trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>): NumberOfLocationsDashboardTileFragmentPresenter {
        return NumberOfLocationsDashboardTileFragmentPresenter(context, trackRecorderServiceController)
    }

    @Provides
    public fun provideDistanceDashboardTileFragmentPresenter(context: Context,
                                                             appSettings: IAppSettings,
                                                             trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>,
                                                             distanceConverterFactory: IDistanceConverterFactory): DistanceDashboardTileFragmentPresenter {
        return DistanceDashboardTileFragmentPresenter(context, appSettings, trackRecorderServiceController, distanceConverterFactory)
    }

    @Provides
    public fun provideBurnedEnergyDashboardTileFragmentPresenter(context: Context,
                                                                 appSettings: IAppSettings,
                                                                 trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>,
                                                                 energyConverterFactory: IEnergyConverterFactory): BurnedEnergyDashboardTileFragmentPresenter {
        return BurnedEnergyDashboardTileFragmentPresenter(context, appSettings, trackRecorderServiceController, energyConverterFactory)
    }

    @Provides
    public fun provideAverageSpeedDashboardTileFragmentPresenter(context: Context,
                                                                 appSettings: IAppSettings,
                                                                 trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>,
                                                                 speedConverterFactory: ISpeedConverterFactory): AverageSpeedDashboardTileFragmentPresenter {
        return AverageSpeedDashboardTileFragmentPresenter(context, appSettings, trackRecorderServiceController, speedConverterFactory)
    }

    @Provides
    public fun provideCurrentSpeedDashboardTileFragmentPresenter(context: Context,
                                                                 appSettings: IAppSettings,
                                                                 trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>,
                                                                 speedConverterFactory: ISpeedConverterFactory): CurrentSpeedDashboardTileFragmentPresenter {
        return CurrentSpeedDashboardTileFragmentPresenter(context, appSettings, trackRecorderServiceController, speedConverterFactory)
    }

    @Provides
    public fun provideMaximumSpeedDashboardTileFragmentPresenter(context: Context,
                                                                 appSettings: IAppSettings,
                                                                 trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>,
                                                                 speedConverterFactory: ISpeedConverterFactory): MaximumSpeedDashboardTileFragmentPresenter {
        return MaximumSpeedDashboardTileFragmentPresenter(context, appSettings, trackRecorderServiceController, speedConverterFactory)
    }
}

