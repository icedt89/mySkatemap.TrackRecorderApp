package com.janhafner.myskatemap.apps.trackrecorder.distancecalculation

import com.janhafner.myskatemap.apps.trackrecorder.common.distanceTo
import com.janhafner.myskatemap.apps.trackrecorder.common.types.Location

public final class DistanceCalculator : IDistanceCalculator {
    public override fun calculateDistance(locations: List<Location>): Float {
        var result = 0.0f

        if(locations.count() < 2) {
            return result
        }

        var lastAccessedLocation: Location = locations.first()
        for(location in locations.drop(1)) {
            result += lastAccessedLocation.distanceTo(location)

            lastAccessedLocation = location
        }

        return result
    }
}

