package com.janhafner.myskatemap.apps.trackrecorder.map.google

import com.janhafner.myskatemap.apps.trackrecorder.map.IMapFeatureInvestigator
import com.janhafner.myskatemap.apps.trackrecorder.map.IMapFeatures

public final class GoogleTrackRecorderMapFeatureInvestigator : IMapFeatureInvestigator {
    public override fun provideMapFeatures(): IMapFeatures {
        return object : IMapFeatures {
            public override val providesNativeMyLocation: Boolean = true

            public override val canAddMarker: Boolean = true
        }
    }
}