package com.janhafner.myskatemap.apps.trackrecorder.modules

import android.content.Context
import com.janhafner.myskatemap.apps.trackrecorder.BuildConfig
import com.janhafner.myskatemap.apps.trackrecorder.isGooglePlayServicesAvailable
import com.janhafner.myskatemap.apps.trackrecorder.map.IMapFeatureInvestigator
import com.janhafner.myskatemap.apps.trackrecorder.map.TrackRecorderMapFragment
import com.janhafner.myskatemap.apps.trackrecorder.map.google.GoogleTrackRecorderMapFeatureInvestigator
import com.janhafner.myskatemap.apps.trackrecorder.map.google.GoogleTrackRecorderMapFragment
import com.janhafner.myskatemap.apps.trackrecorder.map.openstreetmap.OpenStreetMapTrackRecorderMapFeatureInvestigator
import com.janhafner.myskatemap.apps.trackrecorder.map.openstreetmap.OpenStreetMapTrackRecorderMapFragment
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Module()
internal final class MapModule {
    @Provides
    public fun provideTrackRecorderMapFactory(context: Context, appSettings: IAppSettings): TrackRecorderMapFragment {
        if(!BuildConfig.MAP_FORCE_OPENSTREETMAP_MAPCONTROL && appSettings.mapControlTypeName == GoogleTrackRecorderMapFragment::class.java.simpleName) {
            if(context.isGooglePlayServicesAvailable()) {
                return GoogleTrackRecorderMapFragment()
            }
        }

        return OpenStreetMapTrackRecorderMapFragment()
    }

    @Provides
    public fun provideMapFeatureInvestigator(context: Context,
                                             appSettings: IAppSettings,
                                             @Named("GoogleTrackRecorderMapFeatureInvestigator") googleTrackRecorderMapFeatureInvestigator: IMapFeatureInvestigator,
                                             @Named("OpenStreetMapTrackRecorderMapFeatureInvestigator") openStreetMapTrackRecorderMapFeatureInvestigator: IMapFeatureInvestigator): IMapFeatureInvestigator {
        if (!BuildConfig.MAP_FORCE_OPENSTREETMAP_MAPCONTROL && appSettings.mapControlTypeName == GoogleTrackRecorderMapFragment::class.java.simpleName) {
            if (context.isGooglePlayServicesAvailable()) {
                return googleTrackRecorderMapFeatureInvestigator
            }
        }

        return openStreetMapTrackRecorderMapFeatureInvestigator
    }

    @Singleton
    @Provides
    @Named("GoogleTrackRecorderMapFeatureInvestigator")
    public fun provideGoogleTrackRecorderMapFeatureInvestigator(): IMapFeatureInvestigator {
        return GoogleTrackRecorderMapFeatureInvestigator()
    }

    @Singleton
    @Provides
    @Named("OpenStreetMapTrackRecorderMapFeatureInvestigator")
    public fun provideOpenStreetMapTrackRecorderMapFeatureInvestigator(): IMapFeatureInvestigator {
        return OpenStreetMapTrackRecorderMapFeatureInvestigator()
    }
}