package com.janhafner.myskatemap.apps.trackrecorder

import android.content.Context
import android.content.SharedPreferences
import android.location.LocationManager
import android.nfc.NfcAdapter
import android.preference.PreferenceManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.JodaTimeDateTimeMoshaAdapter
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.JodaTimePeriodMoshaAdapter
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.JsonRestApiClient
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.UuidMoshaAdapter
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance.ITrackDistanceUnitFormatterFactory
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance.TrackDistanceCalculator
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance.TrackDistanceUnitFormatterFactory
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.gpx.GpxFileWriter
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.gpx.GpxTrackWriter
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.gpx.IGpxFileWriter
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.gpx.IGpxTrackWriter
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.FileSystemDirectoryNavigator
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.IDirectoryNavigator
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.live.FakeLiveLocationTrackingService
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.live.ILiveLocationTrackingService
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.live.LiveLocationTrackingService
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.settings.AppConfig
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.settings.AppSettings
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.settings.IAppConfig
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.location.Location
import com.janhafner.myskatemap.apps.trackrecorder.location.LocationAvailabilityChangedBroadcastReceiver
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.ServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider.FusedLocationProvider
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider.ILocationProvider
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider.LegacyLocationProvider
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider.TestLocationProvider
import com.janhafner.myskatemap.apps.trackrecorder.views.map.ITrackRecorderMapFragmentFactory
import com.janhafner.myskatemap.apps.trackrecorder.views.map.TrackRecorderMapFragmentFactory
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
internal final class ApplicationModule(private val applicationContext: Context) {
    @Singleton
    @Provides
    public fun provideLocationManager() : LocationManager {
        return this.applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    @Singleton
    @Provides
    public fun provideFusedLocationProviderClient() : FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(this.applicationContext)
    }

    @Provides
    public fun provideLocationProvider(fusedLocationProviderClient: FusedLocationProviderClient, locationManager: LocationManager, appSettings: IAppSettings): ILocationProvider {
        val locationProviderTypeName = appSettings.locationProviderTypeName

        if (locationProviderTypeName == TestLocationProvider::class.java.name) {
            val initialLocation = Location(-1)

            initialLocation.bearing = 1.0f
            initialLocation.latitude = 50.8333
            initialLocation.longitude = 12.9167

            return TestLocationProvider(this.applicationContext, initialLocation, interval = 500)
        }

        if (locationProviderTypeName == LegacyLocationProvider::class.java.name) {
            return LegacyLocationProvider(locationManager)
        }

        return FusedLocationProvider(fusedLocationProviderClient)
    }

    @Provides
    @Singleton
    public fun provideLiveLocationTrackingService(jsonRestApiClient: JsonRestApiClient, appConfig: IAppConfig) : ILiveLocationTrackingService {
        if(appConfig.useFakeLiveLocationTrackingService) {
            return FakeLiveLocationTrackingService()
        }

        return LiveLocationTrackingService(jsonRestApiClient)
    }

    @Provides
    @Singleton
    public fun provideJsonRestApiClient(moshi: Moshi) : JsonRestApiClient {
        val okHttpClient = OkHttpClient.Builder()
                .build()

        return JsonRestApiClient(okHttpClient, moshi)
    }

    @Singleton
    @Provides
    public fun provideGpxFileWriter(gpxTrackWriter: IGpxTrackWriter): IGpxFileWriter {
        return GpxFileWriter(gpxTrackWriter)
    }

    @Singleton
    @Provides
    public fun provideGpxTrackWriter(): IGpxTrackWriter {
        return GpxTrackWriter()
    }

    @Singleton
    @Provides
    public fun provideNfcAdapter(): NfcAdapter? {
        return NfcAdapter.getDefaultAdapter(this.applicationContext)
    }

    @Singleton
    @Provides
    public fun provideAppConfig(moshi: Moshi): IAppConfig {
        return AppConfig.fromAppConfigJson(this.applicationContext, moshi)
    }

    @Provides
    @Singleton
    public fun provideTrackRecorderMapFactory(appConfig: IAppConfig): ITrackRecorderMapFragmentFactory {
        return TrackRecorderMapFragmentFactory(this.applicationContext, appConfig)
    }

    @Singleton
    @Provides
    public fun provideLocationAvailabilityChangedBroadcastReceiver(): LocationAvailabilityChangedBroadcastReceiver {
        return LocationAvailabilityChangedBroadcastReceiver(this.applicationContext)
    }

    @Provides
    @Singleton
    public fun provideTrackService(appBaseDirectoryNavigator: IDirectoryNavigator, moshi: Moshi): ITrackService {
        return TrackService(appBaseDirectoryNavigator, moshi)
    }

    @Provides
    @Singleton
    public fun provideAppBaseDirectoryNavigator(): IDirectoryNavigator {
        val baseDirectory = this.applicationContext.filesDir

        return FileSystemDirectoryNavigator.baseDirectory(baseDirectory)
    }

    @Provides
    @Singleton
    public fun provideApplicationContext(): Context {
        return this.applicationContext
    }

    @Provides
    public fun provideTrackRecorderServiceController(): ServiceController<TrackRecorderServiceBinder>  {
        return ServiceController(this.applicationContext)
    }

    @Provides
    @Singleton
    public fun provideAppSettings(sharedPreferences: SharedPreferences): IAppSettings {
        return AppSettings().bindToSharedPreferences(sharedPreferences)
    }

    @Provides
    @Singleton
    public fun provideSharedPreferences() : SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(this.applicationContext)
    }

    @Singleton
    @Provides
    public fun provideMoshi(): Moshi {
        return Moshi.Builder()
                .add(JodaTimeDateTimeMoshaAdapter())
                .add(JodaTimePeriodMoshaAdapter())
                .add(UuidMoshaAdapter())
                //.add(KotlinJsonAdapterFactory())
                .build()
    }

    @Provides
    public fun provideTrackDistanceCalculator(): TrackDistanceCalculator {
        return TrackDistanceCalculator()
    }

    @Provides
    @Singleton
    public fun provideTrackDistanceUnitFormatterFactory(appSettings: IAppSettings): ITrackDistanceUnitFormatterFactory {
        return TrackDistanceUnitFormatterFactory(appSettings, this.applicationContext)
    }
}