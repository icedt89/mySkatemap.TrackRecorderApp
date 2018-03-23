package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance

import android.content.Context
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.roundTrackDistanceForDisplay

internal final class KilometersTrackDistanceUnitFormatter(private val context: Context) : ITrackDistanceUnitFormatter {
    public override fun format(distanceInMeters: Float): String {
        if (distanceInMeters > KilometersTrackDistanceUnitFormatter.MetersToKilometersFactor) {
            return this.context.getString(R.string.app_trackdistance_template_kilometers, (distanceInMeters / KilometersTrackDistanceUnitFormatter.MetersToKilometersFactor).roundTrackDistanceForDisplay(this.context))
        }

        return this.context.getString(R.string.app_trackdistance_template_meters, distanceInMeters.roundTrackDistanceForDisplay(this.context))
    }

    companion object {
        private const val MetersToKilometersFactor: Float = 1000.0f
    }
}