package com.janhafner.myskatemap.apps.trackrecorder.modules

import com.janhafner.myskatemap.apps.trackrecorder.map.IMapFeatureInvestigator
import com.janhafner.myskatemap.apps.trackrecorder.map.google.GoogleTrackRecorderMapFeatureInvestigator
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Module()
internal final class MapModule {
    @Provides
    public fun provideMapFeatureInvestigator(@Named("GoogleTrackRecorderMapFeatureInvestigator") googleTrackRecorderMapFeatureInvestigator: IMapFeatureInvestigator): IMapFeatureInvestigator {
        return googleTrackRecorderMapFeatureInvestigator
    }

    @Singleton
    @Provides
    @Named("GoogleTrackRecorderMapFeatureInvestigator")
    public fun provideGoogleTrackRecorderMapFeatureInvestigator(): IMapFeatureInvestigator {
        return GoogleTrackRecorderMapFeatureInvestigator()
    }
}