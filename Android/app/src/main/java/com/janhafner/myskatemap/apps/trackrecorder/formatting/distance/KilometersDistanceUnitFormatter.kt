package com.janhafner.myskatemap.apps.trackrecorder.formatting.distance

import com.janhafner.myskatemap.apps.trackrecorder.formatDistanceKilometers
import com.janhafner.myskatemap.apps.trackrecorder.formatDistanceMeters

internal final class KilometersDistanceUnitFormatter : IDistanceUnitFormatter {
    public override fun format(distanceInMeters: Float): String {
        if (distanceInMeters > METERS_TO_KILOMETERS_CONVERSION_FACTOR) {
            return (distanceInMeters / METERS_TO_KILOMETERS_CONVERSION_FACTOR).formatDistanceKilometers()
        }

        return distanceInMeters.formatDistanceMeters()
    }

    companion object {
        private const val METERS_TO_KILOMETERS_CONVERSION_FACTOR: Float = 1000.0f
    }
}