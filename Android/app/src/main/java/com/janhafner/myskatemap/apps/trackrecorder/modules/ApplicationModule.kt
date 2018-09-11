package com.janhafner.myskatemap.apps.trackrecorder.modules

import android.content.Context
import android.location.LocationManager
import com.couchbase.lite.DatabaseConfiguration
import com.google.android.gms.location.ActivityRecognitionClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.janhafner.myskatemap.apps.trackrecorder.JsonRestApiClient
import com.janhafner.myskatemap.apps.trackrecorder.jodatime.JodaTimeDateTimeMoshiAdapter
import com.janhafner.myskatemap.apps.trackrecorder.jodatime.JodaTimePeriodMoshiAdapter
import com.janhafner.myskatemap.apps.trackrecorder.jodatime.UuidMoshiAdapter
import com.janhafner.myskatemap.apps.trackrecorder.services.ICrudRepository
import com.janhafner.myskatemap.apps.trackrecorder.services.activitydetection.ActivityDetectorBroadcastReceiverFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.activitydetection.IActivityDetectorBroadcastReceiverFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.burnedenergy.IMetActivityDefinitionFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.burnedenergy.MetActivityDefinitionFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.couchdb.CouchDbDashboardService
import com.janhafner.myskatemap.apps.trackrecorder.services.couchdb.CouchDbFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.couchdb.CouchDbTrackService
import com.janhafner.myskatemap.apps.trackrecorder.services.couchdb.ICouchDbFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.live.ILiveLocationTrackingServiceFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.live.LiveLocationTrackingServiceFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.locationavailability.ILocationAvailabilityChangedDetector
import com.janhafner.myskatemap.apps.trackrecorder.services.locationavailability.LocationAvailabilityChangedBroadcastReceiver
import com.janhafner.myskatemap.apps.trackrecorder.services.models.Dashboard
import com.janhafner.myskatemap.apps.trackrecorder.services.models.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.IServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.ServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderService
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
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

@Module(includes = [SystemServicesModule::class, ConversionModule::class, ExportModule::class])
internal final class ApplicationModule(private val applicationContext: Context) {
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
    public fun provideStillDetectorBroadcastReceiverFactory(activityRecognitionClient: ActivityRecognitionClient, appSettings: IAppSettings) : IActivityDetectorBroadcastReceiverFactory {
        return ActivityDetectorBroadcastReceiverFactory(this.applicationContext, activityRecognitionClient,appSettings )
    }

    @Provides
    @Singleton
    public fun provideLocationProviderFactory(fusedLocationProviderClient: FusedLocationProviderClient, locationManager: LocationManager, appSettings: IAppSettings) : ILocationProviderFactory {
        return LocationProviderFactory(this.applicationContext, appSettings, fusedLocationProviderClient, locationManager)
    }

    @Provides
    @Singleton
    public fun provideLiveLocationTrackingServiceFactory(jsonRestApiClient: JsonRestApiClient, appSettings: IAppSettings) : ILiveLocationTrackingServiceFactory {
        return LiveLocationTrackingServiceFactory(appSettings, jsonRestApiClient)
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
    public fun provideTrackRecorderMapFactory(appSettings: IAppSettings): ITrackRecorderMapFragmentFactory {
        return TrackRecorderMapFragmentFactory(this.applicationContext, appSettings)
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
    public fun provideUserProfile(): IUserProfileSettings {
        var sharedPreferences = this.applicationContext.getSharedPreferences("userprofilesettings", Context.MODE_PRIVATE)

        sharedPreferences = FixedTypeConversionSharedPreferencesAdapter(sharedPreferences)

        return UserProfileSettings().bindToSharedPreferences(sharedPreferences, this.applicationContext)
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
    public fun provideDashboardService(@Named("dashboards-couchdb-factory") dashboardsCouchDbFactory: ICouchDbFactory) : ICrudRepository<Dashboard> {
        return CouchDbDashboardService(dashboardsCouchDbFactory)
    }

    @Provides
    @Singleton
    public fun provideTrackRecordingService(@Named("track-recordings-couchdb-factory") trackRecordingsCouchDbFactory: ICouchDbFactory, appSettings: IAppSettings) : ICrudRepository<TrackRecording> {
        return CouchDbTrackService(trackRecordingsCouchDbFactory, appSettings)
    }
}

