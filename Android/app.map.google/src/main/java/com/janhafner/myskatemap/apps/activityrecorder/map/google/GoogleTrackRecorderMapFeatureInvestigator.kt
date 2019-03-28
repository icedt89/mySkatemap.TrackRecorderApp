package com.janhafner.myskatemap.apps.activityrecorder.map.google

import com.janhafner.myskatemap.apps.activityrecorder.map.IMapFeatureInvestigator
import com.janhafner.myskatemap.apps.activityrecorder.map.IMapFeatures

public final class GoogleTrackRecorderMapFeatureInvestigator : IMapFeatureInvestigator {
    public override fun provideMapFeatures(): IMapFeatures {
        return object : IMapFeatures {
            public override val providesNativeMyLocation: Boolean = true

            public override val canAddMarker: Boolean = true
        }
    }
}