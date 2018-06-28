package com.janhafner.myskatemap.apps.trackrecorder.services.distance

import android.content.Context
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.roundTrackDistanceForDisplay

internal final class KilometersTrackDistanceUnitFormatter(private val context: Context) : ITrackDistanceUnitFormatter {
    public override fun format(distanceInMeters: Float): String {
        if (distanceInMeters > KilometersTrackDistanceUnitFormatter.METERS_TO_KILOMETERS_CONVERSION_FACTOR) {
            return "${(distanceInMeters / KilometersTrackDistanceUnitFormatter.METERS_TO_KILOMETERS_CONVERSION_FACTOR).roundTrackDistanceForDisplay(this.context)} km"
        }

        return "${distanceInMeters.roundTrackDistanceForDisplay(this.context)} m"
    }

    companion object {
        private const val METERS_TO_KILOMETERS_CONVERSION_FACTOR: Float = 1000.0f
    }
}