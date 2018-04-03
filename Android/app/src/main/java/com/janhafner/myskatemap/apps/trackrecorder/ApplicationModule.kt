package com.janhafner.myskatemap.apps.trackrecorder

import android.content.Context
import android.preference.PreferenceManager
import com.janhafner.myskatemap.apps.trackrecorder.data.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.JodaTimeDateTimeMoshaAdapter
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.JodaTimePeriodMoshaAdapter
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.UuidMoshaAdapter
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance.ITrackDistanceUnitFormatterFactory
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance.TrackDistanceCalculator
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance.TrackDistanceUnitFormatterFactory
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.CurrentTrackRecordingStore
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.IFileBasedDataStore
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.settings.AppSettings
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.location.Location
import com.janhafner.myskatemap.apps.trackrecorder.location.LocationAvailabilityChangedBroadcastReceiver
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider.FusedLocationProvider
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider.ILocationProvider
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider.LegacyLocationProvider
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider.TestLocationProvider
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.ITrackRecorderActivityPresenter
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.TrackRecorderActivityPresenter
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
internal final class ApplicationModule(private val applicationContext: Context) {
    @Singleton
    @Provides
    public fun provideLocationProvider(): ILocationProvider {
        val locationProviderTypeName = TestLocationProvider::class.java.name

        if (locationProviderTypeName == TestLocationProvider::class.java.name) {
            val initialLocation = Location(-1)

            initialLocation.bearing = 1.0f
            initialLocation.latitude = 50.8333
            initialLocation.longitude = 12.9167

            return TestLocationProvider(this.applicationContext, initialLocation, interval = 500)
        }

        if (locationProviderTypeName == LegacyLocationProvider::class.java.name) {
            return LegacyLocationProvider(this.applicationContext)
        }

        return FusedLocationProvider(this.applicationContext)
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

    @Provides
    @Singleton
    public fun provideTrackRecorderActivityPresenter(currentTrackRecordingStore: IFileBasedDataStore<TrackRecording>): ITrackRecorderActivityPresenter {
        return TrackRecorderActivityPresenter(currentTrackRecordingStore)
    }

    @Provides
    @Singleton
    public fun provideAppSettings(): IAppSettings {
        val sharedPreferences  =PreferenceManager.getDefaultSharedPreferences(this.applicationContext)

        val appSettings = AppSettings.bindToSharedPreferences(sharedPreferences)

        return appSettings
    }

    @Singleton
    @Provides
    public fun provideCurrentTrackRecordingStore(moshi: Moshi): IFileBasedDataStore<TrackRecording> {
        return CurrentTrackRecordingStore(this.applicationContext, moshi)
    }

    @Singleton
    @Provides
    public fun provideGson(): Moshi {
        return Moshi.Builder()
                .add(JodaTimeDateTimeMoshaAdapter())
                .add(JodaTimePeriodMoshaAdapter())
                .add(UuidMoshaAdapter())
                //.add(KotlinJsonAdapterFactory())
                .build()
    }

    @Provides
    @Singleton
    public fun provideTrackDistanceCalculator(): TrackDistanceCalculator {
        return TrackDistanceCalculator()
    }

    @Provides
    @Singleton
    public fun provideTrackDistanceUnitFormatterFactory(appSettings: IAppSettings): ITrackDistanceUnitFormatterFactory {
        return TrackDistanceUnitFormatterFactory(appSettings, this.applicationContext)
    }
}