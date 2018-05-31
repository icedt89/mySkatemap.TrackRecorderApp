package com.janhafner.myskatemap.apps.trackrecorder

import android.content.Context
import android.content.SharedPreferences
import android.location.LocationManager
import android.preference.PreferenceManager
import com.couchbase.lite.Database
import com.couchbase.lite.DatabaseConfiguration
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityRecognitionClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.janhafner.myskatemap.apps.trackrecorder.io.FileSystemDirectoryNavigator
import com.janhafner.myskatemap.apps.trackrecorder.io.IDirectoryNavigator
import com.janhafner.myskatemap.apps.trackrecorder.io.gpx.GpxFileWriter
import com.janhafner.myskatemap.apps.trackrecorder.io.gpx.GpxTrackWriter
import com.janhafner.myskatemap.apps.trackrecorder.io.gpx.IGpxFileWriter
import com.janhafner.myskatemap.apps.trackrecorder.io.gpx.IGpxTrackWriter
import com.janhafner.myskatemap.apps.trackrecorder.jodatime.JodaTimeDateTimeMoshiAdapter
import com.janhafner.myskatemap.apps.trackrecorder.jodatime.JodaTimePeriodMoshiAdapter
import com.janhafner.myskatemap.apps.trackrecorder.jodatime.UuidMoshiAdapter
import com.janhafner.myskatemap.apps.trackrecorder.services.CouchDbTrackService
import com.janhafner.myskatemap.apps.trackrecorder.services.ITrackService
import com.janhafner.myskatemap.apps.trackrecorder.services.calories.IMetActivityDefinitionFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.calories.MetActivityDefinitionFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.distance.ITrackDistanceUnitFormatterFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.distance.TrackDistanceUnitFormatterFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.live.ILiveLocationTrackingServiceFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.live.LiveLocationTrackingServiceFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.stilldetection.StillDetectorBroadcastReceiver
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.LocationAvailabilityChangedBroadcastReceiver
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.ServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider.ILocationProviderFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider.LocationProviderFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.refactored.RefactoredTrackRecorderService
import com.janhafner.myskatemap.apps.trackrecorder.settings.AppConfig
import com.janhafner.myskatemap.apps.trackrecorder.settings.AppSettings
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppConfig
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
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
    public fun provideActivityRecognitionClient(): ActivityRecognitionClient {
        return ActivityRecognition.getClient(this.applicationContext)
    }

    @Singleton
    @Provides
    public fun provideMetActivityDefinitionFactory(moshi: Moshi) : IMetActivityDefinitionFactory {
        return MetActivityDefinitionFactory(this.applicationContext, moshi)
    }

    @Singleton
    @Provides
    public fun provideFusedLocationProviderClient() : FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(this.applicationContext)
    }

    @Singleton
    @Provides
    public fun provideStillDetector(activityRecognitionClient: ActivityRecognitionClient) : StillDetectorBroadcastReceiver {
        return StillDetectorBroadcastReceiver(this.applicationContext, activityRecognitionClient)
    }

    @Provides
    @Singleton
    public fun provideLocationProviderFactory(fusedLocationProviderClient: FusedLocationProviderClient, locationManager: LocationManager, appSettings: IAppSettings) : ILocationProviderFactory {
        return LocationProviderFactory(this.applicationContext, appSettings, fusedLocationProviderClient, locationManager)
    }

    @Provides
    @Singleton
    public fun provideLiveLocationTrackingServiceFactory(jsonRestApiClient: JsonRestApiClient, appConfig: IAppConfig, appSettings: IAppSettings) : ILiveLocationTrackingServiceFactory {
        return LiveLocationTrackingServiceFactory(appConfig, appSettings, jsonRestApiClient)
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
    public fun provideTrackService(appBaseDirectoryNavigator: IDirectoryNavigator, couchDb: Database, appSettings: IAppSettings): ITrackService {
        return CouchDbTrackService(couchDb, appBaseDirectoryNavigator, appSettings)
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
    public fun provideTrackRecorderServiceController(): ServiceController<RefactoredTrackRecorderService, TrackRecorderServiceBinder>  {
        return ServiceController(this.applicationContext, RefactoredTrackRecorderService::class.java)
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
                .add(JodaTimeDateTimeMoshiAdapter())
                .add(JodaTimePeriodMoshiAdapter())
                .add(UuidMoshiAdapter())
                // .add(KotlinJsonAdapterFactory())
                .build()
    }

    @Provides
    @Singleton
    public fun provideTrackDistanceUnitFormatterFactory(appSettings: IAppSettings): ITrackDistanceUnitFormatterFactory {
        return TrackDistanceUnitFormatterFactory(appSettings, this.applicationContext)
    }

    @Provides
    @Singleton
    public fun provideCouchDb() : Database {
        val databaseConfiguration = DatabaseConfiguration(this.applicationContext)

        return Database("track-recordings", databaseConfiguration)
    }
}