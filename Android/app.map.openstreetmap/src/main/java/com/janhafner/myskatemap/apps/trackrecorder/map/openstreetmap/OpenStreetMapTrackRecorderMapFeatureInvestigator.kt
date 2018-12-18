package com.janhafner.myskatemap.apps.trackrecorder.map.openstreetmap

import com.janhafner.myskatemap.apps.trackrecorder.map.IMapFeatureInvestigator
import com.janhafner.myskatemap.apps.trackrecorder.map.IMapFeatures

public final class OpenStreetMapTrackRecorderMapFeatureInvestigator : IMapFeatureInvestigator {
    public override fun provideMapFeatures(): IMapFeatures {
        return object : IMapFeatures {
            public override val providesNativeMyLocation: Boolean = true

            public override val canAddMarker: Boolean = true
        }
    }
}