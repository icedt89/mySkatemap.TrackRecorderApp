package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance

import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.settings.IAppSettings
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
internal final class TrackDistanceModule(@Deprecated("Inject using Dagger") private val appSettings: IAppSettings) {

    @Provides
    @Singleton
    public fun provideTrackDistanceCalculator(): TrackDistanceCalculator {
        return TrackDistanceCalculator()
    }

    @Provides
    @Singleton
    public fun provideTrackDistanceUnitFormatterFactory(): ITrackDistanceUnitFormatterFactory {
        return TrackDistanceUnitFormatterFactory(this.appSettings)
    }
}