package com.janhafner.myskatemap.apps.trackrecorder.formatting.distance

import com.janhafner.myskatemap.apps.trackrecorder.formatDistanceMiles
import com.janhafner.myskatemap.apps.trackrecorder.formatDistanceMeters

internal final class MilesDistanceUnitFormatter : IDistanceUnitFormatter {
    public override fun format(distanceInMeters: Float): String {
        if (distanceInMeters > METERS_TO_MILES_CONVERSION_FACTOR) {
            return (distanceInMeters / METERS_TO_MILES_CONVERSION_FACTOR).formatDistanceMiles()
        }

        return distanceInMeters.formatDistanceMeters()
    }

    companion object {
        private const val METERS_TO_MILES_CONVERSION_FACTOR: Float = 1609.344f
    }
}