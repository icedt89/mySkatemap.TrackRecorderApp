package com.janhafner.myskatemap.apps.trackrecorder.modules

import android.content.Context
import android.location.LocationManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.janhafner.myskatemap.apps.trackrecorder.BuildConfig
import com.janhafner.myskatemap.apps.trackrecorder.common.types.Location
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.ITrackRecorderMapFragmentFactory
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.TrackRecorderMapFragmentFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.IServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.ServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderService
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider.*
import com.janhafner.myskatemap.apps.trackrecorder.settings.*
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [
    SystemServicesModule::class,
    ConversionModule::class,
    ExportModule::class,
    TrackModule::class,
    DashboardModule::class,
    ActivityDetectionModule::class,
    LocationAvailabilityModule::class,
    DistanceCalculationModule::class,
    BurnedEnergyModule::class,
    LiveLocationModule::class])
internal final class ApplicationModule(private val applicationContext: Context) {
    @Singleton
    @Provides
        public fun providerMyCurrentLocationProvider(context: Context, locationManager: LocationManager, fusedLocationProviderClient: FusedLocationProviderClient): IMyLocationProvider {
            if (BuildConfig.LOCATION_PROVIDER_USE_SIMULATED_LOCATION_PROVIDER) {
                return SimulatedMyLocationProvider()
            }

        return FusedMyLocationProvider(context, fusedLocationProviderClient)
        // return LegacyMyLocationProvider(context, locationManager)
    }

    @Provides
    public fun provideLocationProvider(context: Context, fusedLocationProviderClient: FusedLocationProviderClient): ILocationProvider {
        if (BuildConfig.LOCATION_PROVIDER_USE_SIMULATED_LOCATION_PROVIDER) {
            val initialLocation = Location()

            initialLocation.bearing = BuildConfig.SIMULATED_LOCATION_PROVIDER_INITIAL_BEARING
            initialLocation.latitude = BuildConfig.MAP_INITIAL_LATITUDE
            initialLocation.longitude = BuildConfig.MAP_INITIAL_LONGITUDE
            initialLocation.altitude = BuildConfig.MAP_INITIAL_ALTITUDE

            return SimulatedLocationProvider(context, initialLocation,
                    BuildConfig.SIMULATED_LOCATION_PROVIDER_BEARING_STEPPING,
                    BuildConfig.SIMULATED_LOCATION_PROVIDER_LATITUDE_STEPPING,
                    BuildConfig.SIMULATED_LOCATION_PROVIDER_LONGITUDE_STEPPING,
                    BuildConfig.SIMULATED_LOCATION_PROVIDER_DELAY_IN_MILLISECONDS,
                    BuildConfig.SIMULATED_LOCATION_PROVIDER_INTERVAL_IN_MILLISECONDS,
                    BuildConfig.SIMULATED_LOCATION_PROVIDER_FORCE_NEED_OF_LOCATION_SERVICES)
        }

        return FusedLocationProvider(context, fusedLocationProviderClient,
                BuildConfig.FUSED_LOCATION_PROVIDER_FASTEST_INTERVAL_IN_MILLISECONDS,
                BuildConfig.FUSED_LOCATION_PROVIDER_INTERVAL_IN_MILLISECONDS,
                BuildConfig.FUSED_LOCATION_PROVIDER_MAX_WAIT_TIME_IN_MILLISECONDS,
                BuildConfig.FUSED_LOCATION_PROVIDER_SMALLEST_DISPLACEMENT_IN_METERS)
    }

    @Singleton
    @Provides
    public fun provideTrackRecorderMapFactory(context: Context, appSettings: IAppSettings): ITrackRecorderMapFragmentFactory {
        return TrackRecorderMapFragmentFactory(context, appSettings)
    }

    @Provides
    @Singleton
    public fun provideApplicationContext(): Context {
        return this.applicationContext
    }

    @Singleton
    @Provides
    public fun provideTrackRecorderServiceController(context: Context): IServiceController<TrackRecorderServiceBinder> {
        return ServiceController(context, TrackRecorderService::class.java)
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