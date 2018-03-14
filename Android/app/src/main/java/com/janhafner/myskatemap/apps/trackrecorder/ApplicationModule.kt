package com.janhafner.myskatemap.apps.trackrecorder

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.janhafner.myskatemap.apps.trackrecorder.data.HistoricTrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.data.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance.ITrackDistanceUnitFormatterFactory
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance.TrackDistanceCalculator
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance.TrackDistanceUnitFormatterFactory
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.gson.JodaTimeDateTimeGsonAdapter
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.gson.JodaTimePeriodGsonAdapter
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.CurrentTrackRecordingStore
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.IFileBasedDataStore
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.TrackRecordingHistoryStore
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.settings.AppSettings
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.location.Location
import com.janhafner.myskatemap.apps.trackrecorder.location.LocationAvailabilityChangedBroadcastReceiver
import com.janhafner.myskatemap.apps.trackrecorder.location.provider.FusedLocationProvider
import com.janhafner.myskatemap.apps.trackrecorder.location.provider.ILocationProvider
import com.janhafner.myskatemap.apps.trackrecorder.location.provider.LegacyLocationProvider
import com.janhafner.myskatemap.apps.trackrecorder.location.provider.TestLocationProvider
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
            val initialLocation: Location = Location(-1)

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
    public fun provideAppSettings(): IAppSettings {
        return AppSettings()
    }

    @Singleton
    @Provides
    public fun provideCurrentTrackRecordingStore(): IFileBasedDataStore<TrackRecording> {
        return CurrentTrackRecordingStore(this.applicationContext)
    }

    @Singleton
    @Provides
    public fun provideTrackRecordingHistoryStore(): IFileBasedDataStore<List<HistoricTrackRecording>> {
        return TrackRecordingHistoryStore(this.applicationContext)
    }

    @Singleton
    @Provides
    public fun providesGson(): Gson {
        return GsonBuilder()
                .registerTypeAdapter(JodaTimeDateTimeGsonAdapter::class.java, JodaTimeDateTimeGsonAdapter())
                .registerTypeAdapter(JodaTimePeriodGsonAdapter::class.java, JodaTimePeriodGsonAdapter())
                .create()
    }

    @Provides
    @Singleton
    public fun providesTrackDistanceCalculator(): TrackDistanceCalculator {
        return TrackDistanceCalculator()
    }

    @Provides
    @Singleton
    public fun providesTrackDistanceUnitFormatterFactory(appSettings: IAppSettings): ITrackDistanceUnitFormatterFactory {
        return TrackDistanceUnitFormatterFactory(appSettings)
    }
}