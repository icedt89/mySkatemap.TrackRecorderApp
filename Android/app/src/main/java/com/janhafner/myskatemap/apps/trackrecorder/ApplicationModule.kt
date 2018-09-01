package com.janhafner.myskatemap.apps.trackrecorder

import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.hardware.SensorManager
import android.location.LocationManager
import android.preference.PreferenceManager
import android.util.EventLog
import com.couchbase.lite.Database
import com.couchbase.lite.DatabaseConfiguration
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityRecognitionClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.janhafner.myskatemap.apps.trackrecorder.formatting.FixedTypeConversionSharedPreferencesAdapter
import com.janhafner.myskatemap.apps.trackrecorder.formatting.burnedenergy.BurnedEnergyUnitFormatterFactory
import com.janhafner.myskatemap.apps.trackrecorder.formatting.burnedenergy.IBurnedEnergyUnitFormatterFactory
import com.janhafner.myskatemap.apps.trackrecorder.formatting.distance.DistanceUnitFormatterFactory
import com.janhafner.myskatemap.apps.trackrecorder.formatting.distance.IDistanceUnitFormatterFactory
import com.janhafner.myskatemap.apps.trackrecorder.formatting.speed.ISpeedUnitFormatterFactory
import com.janhafner.myskatemap.apps.trackrecorder.formatting.speed.SpeedUnitFormatterFactory
import com.janhafner.myskatemap.apps.trackrecorder.io.FileSystemDirectoryNavigator
import com.janhafner.myskatemap.apps.trackrecorder.io.IDirectoryNavigator
import com.janhafner.myskatemap.apps.trackrecorder.io.gpx.GpxFileWriter
import com.janhafner.myskatemap.apps.trackrecorder.io.gpx.GpxTrackWriter
import com.janhafner.myskatemap.apps.trackrecorder.io.gpx.IGpxFileWriter
import com.janhafner.myskatemap.apps.trackrecorder.io.gpx.IGpxTrackWriter
import com.janhafner.myskatemap.apps.trackrecorder.jodatime.JodaTimeDateTimeMoshiAdapter
import com.janhafner.myskatemap.apps.trackrecorder.jodatime.JodaTimePeriodMoshiAdapter
import com.janhafner.myskatemap.apps.trackrecorder.jodatime.UuidMoshiAdapter
import com.janhafner.myskatemap.apps.trackrecorder.services.*
import com.janhafner.myskatemap.apps.trackrecorder.services.calories.IMetActivityDefinitionFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.calories.MetActivityDefinitionFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.dashboard.Dashboard
import com.janhafner.myskatemap.apps.trackrecorder.services.dashboard.DashboardService
import com.janhafner.myskatemap.apps.trackrecorder.services.live.ILiveLocationTrackingServiceFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.live.LiveLocationTrackingServiceFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.stilldetection.IStillDetector
import com.janhafner.myskatemap.apps.trackrecorder.services.stilldetection.StillDetectorBroadcastReceiver
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.*
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider.ILocationProviderFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider.LocationProviderFactory
import com.janhafner.myskatemap.apps.trackrecorder.settings.*
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.DashboardTileFragmentFactory
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles.IDashboardTileFragmentFactory
import com.janhafner.myskatemap.apps.trackrecorder.views.map.ITrackRecorderMapFragmentFactory
import com.janhafner.myskatemap.apps.trackrecorder.views.map.TrackRecorderMapFragmentFactory
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import okhttp3.Call
import okhttp3.EventListener
import okhttp3.OkHttpClient
import java.io.IOException
import javax.inject.Named
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
    public fun provideDashboardTileFragmentFactory() : IDashboardTileFragmentFactory {
        return DashboardTileFragmentFactory()
    }

    @Singleton
    @Provides
    public fun provideMetActivityDefinitionFactory(moshi: Moshi) : IMetActivityDefinitionFactory {
        return MetActivityDefinitionFactory(this.applicationContext, moshi)
    }

    @Singleton
    @Provides
    public fun provideNotificationManager() : NotificationManager {
        return this.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    @Singleton
    @Provides
    public fun provideSensorManager() : SensorManager {
        return this.applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    @Singleton
    @Provides
    public fun provideFusedLocationProviderClient() : FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(this.applicationContext)
    }

    @Singleton
    @Provides
    public fun provideStillDetectorBroadcastReceiver(activityRecognitionClient: ActivityRecognitionClient) : StillDetectorBroadcastReceiver {
        return StillDetectorBroadcastReceiver(this.applicationContext, activityRecognitionClient)
    }

    @Singleton
    @Provides
    public fun provideStillDetector(stillDetectorBroadcastReceiver: StillDetectorBroadcastReceiver) : IStillDetector {
        return stillDetectorBroadcastReceiver
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
                // TODO: Working?
                .eventListener(object : EventListener() {
                    override fun callFailed(call: Call, ioe: IOException) {
                        super.callFailed(call, ioe)
                    }

                    override fun callStart(call: Call) {
                        super.callStart(call)
                    }

                    override fun callEnd(call: Call) {
                        super.callEnd(call)
                    }
                })
                .build()

        return JsonRestApiClient(okHttpClient, moshi)
    }

    @Singleton
    @Provides
    public fun provideGpxFileWriter(gpxTrackWriter: IGpxTrackWriter, userProfile: IUserProfile): IGpxFileWriter {
        return GpxFileWriter(gpxTrackWriter, this.applicationContext, userProfile)
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

    @Provides
    @Singleton
    public fun provideLocationAvailabilityChangedDetector(locationAvailabilityChangedBroadcastReceiver: LocationAvailabilityChangedBroadcastReceiver) : ILocationAvailabilityChangedDetector {
        return locationAvailabilityChangedBroadcastReceiver
    }

    @Singleton
    @Provides
    public fun provideLocationAvailabilityChangedBroadcastReceiver(): LocationAvailabilityChangedBroadcastReceiver {
        return LocationAvailabilityChangedBroadcastReceiver(this.applicationContext)
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

    @Singleton
    @Provides
    public fun provideTrackRecorderServiceController(): IServiceController<TrackRecorderServiceBinder> {
        return ServiceController(this.applicationContext, TrackRecorderService::class.java)
    }

    @Provides
    @Singleton
    public fun provideAppSettings(): IAppSettings {
        var sharedPreferences = this.applicationContext.getSharedPreferences("appsettings", Context.MODE_PRIVATE)

        sharedPreferences = FixedTypeConversionSharedPreferencesAdapter(sharedPreferences)

        return AppSettings().bindToSharedPreferences(sharedPreferences, this.applicationContext)
    }

    @Provides
    @Singleton
    public fun provideUserProfile(): IUserProfile {
        var sharedPreferences = this.applicationContext.getSharedPreferences("userprofilesettings", Context.MODE_PRIVATE)

        sharedPreferences = FixedTypeConversionSharedPreferencesAdapter(sharedPreferences)

        return UserProfile().bindToSharedPreferences(sharedPreferences, this.applicationContext)
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
    public fun provideDistanceUnitFormatterFactory(appSettings: IAppSettings): IDistanceUnitFormatterFactory {
        return DistanceUnitFormatterFactory(appSettings)
    }

    @Provides
    @Singleton
    public fun provideSpeedUnitFormatterFactory(appSettings: IAppSettings): ISpeedUnitFormatterFactory {
        return SpeedUnitFormatterFactory(appSettings)
    }

    @Provides
    @Singleton
    public fun provideBurnedEnergyUnitFormatterFactory(appSettings: IAppSettings): IBurnedEnergyUnitFormatterFactory {
        return BurnedEnergyUnitFormatterFactory(appSettings)
    }

    @Named("track-recordings-couchdb-factory")
    @Singleton
    @Provides
    public fun provideTrackRecordingsCouchDbFactory(): ICouchDbFactory {
        val databaseConfiguration = DatabaseConfiguration(this.applicationContext)

        return CouchDbFactory("track-recordings", databaseConfiguration)
    }

    @Named("dashboards-couchdb-factory")
    @Singleton
    @Provides
    public fun provideDashboardsCouchDbFactory(): ICouchDbFactory {
        val databaseConfiguration = DatabaseConfiguration(this.applicationContext)

        return CouchDbFactory("dashboards", databaseConfiguration)
    }

    @Provides
    @Singleton
    public fun provideTrackService(appBaseDirectoryNavigator: IDirectoryNavigator, appSettings: IAppSettings, @Named("track-recordings-couchdb-factory") trackRecordingsCouchDbFactory: ICouchDbFactory): ITrackService {
        return TrackService(trackRecordingsCouchDbFactory, appBaseDirectoryNavigator, appSettings)
    }

    @Provides
    @Singleton
    public fun provideDashboardService(@Named("dashboards-couchdb-factory") dashboardsCouchDbFactory: ICouchDbFactory) : ICrudRepository<Dashboard> {
        return DashboardService(dashboardsCouchDbFactory)
    }
}