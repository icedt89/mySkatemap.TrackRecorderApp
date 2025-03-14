package com.janhafner.myskatemap.apps.activityrecorder.modules

import android.content.Context
import android.util.Log
import com.janhafner.myskatemap.apps.activityrecorder.BuildConfig
import com.janhafner.myskatemap.apps.activityrecorder.core.types.Location
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.IServiceController
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.ServiceController
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.ActivityRecorderService
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.ActivityRecorderServiceBinder
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.provider.FusedLocationProvider
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.provider.ILocationProvider
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.provider.SimulatedLocationProvider
import com.janhafner.myskatemap.apps.activityrecorder.settings.*
import dagger.Module
import dagger.Provides
import org.joda.time.DateTime
import javax.inject.Singleton

@Module(includes = [
    ConversionModule::class,
    TrackModule::class,
    DashboardModule::class,
    LiveLocationModule::class,
    EventingModule::class,
    MapModule::class])
internal final class ApplicationModule(private val applicationContext: Context) {
    @Provides
    public fun provideLocationProvider(context: Context): ILocationProvider {
        if (BuildConfig.LOCATION_PROVIDER_USE_SIMULATED_LOCATION_PROVIDER) {
            Log.v("ApplicationModule", "Using SimulatedLocationProvider as location provider")

            val initialLocation = Location("simulated", DateTime.now(), BuildConfig.MAP_INITIAL_LATITUDE, BuildConfig.MAP_INITIAL_LONGITUDE)

            initialLocation.bearing = BuildConfig.SIMULATED_LOCATION_PROVIDER_INITIAL_BEARING
            initialLocation.altitude = BuildConfig.MAP_INITIAL_ALTITUDE

            return SimulatedLocationProvider(context, initialLocation,
                    BuildConfig.SIMULATED_LOCATION_PROVIDER_BEARING_STEPPING,
                    BuildConfig.SIMULATED_LOCATION_PROVIDER_LATITUDE_STEPPING,
                    BuildConfig.SIMULATED_LOCATION_PROVIDER_LONGITUDE_STEPPING,
                    BuildConfig.SIMULATED_LOCATION_PROVIDER_DELAY_IN_MILLISECONDS,
                    BuildConfig.SIMULATED_LOCATION_PROVIDER_INTERVAL_IN_MILLISECONDS)
        }

        Log.v("ApplicationModule", "Using FusedLocationProvider as location provider")

        return FusedLocationProvider(context,
                BuildConfig.FUSED_LOCATION_PROVIDER_FASTEST_INTERVAL_IN_MILLISECONDS,
                BuildConfig.FUSED_LOCATION_PROVIDER_INTERVAL_IN_MILLISECONDS,
                BuildConfig.FUSED_LOCATION_PROVIDER_MAX_WAIT_TIME_IN_MILLISECONDS,
                BuildConfig.FUSED_LOCATION_PROVIDER_SMALLEST_DISPLACEMENT_IN_METERS)
    }

    @Provides
    @Singleton
    public fun provideApplicationContext(): Context {
        return this.applicationContext
    }

    @Singleton
    @Provides
    public fun provideTrackRecorderServiceController(context: Context): IServiceController<ActivityRecorderServiceBinder> {
        return ServiceController(context, ActivityRecorderService::class.java)
    }

    @Provides
    @Singleton
    public fun provideAppSettings(context: Context): IAppSettings {
        var sharedPreferences = context.getSharedPreferences("appsettings", Context.MODE_PRIVATE)

        sharedPreferences = FixedTypeConversionSharedPreferencesAdapter(sharedPreferences)

        return AppSettings().bindToSharedPreferences(sharedPreferences, context)
    }

    @Provides
    @Singleton
    public fun provideUserProfile(context: Context): IUserProfileSettings {
        var sharedPreferences = context.getSharedPreferences("userprofilesettings", Context.MODE_PRIVATE)

        sharedPreferences = FixedTypeConversionSharedPreferencesAdapter(sharedPreferences)

        return UserProfileSettings().bindToSharedPreferences(sharedPreferences, context)
    }
}