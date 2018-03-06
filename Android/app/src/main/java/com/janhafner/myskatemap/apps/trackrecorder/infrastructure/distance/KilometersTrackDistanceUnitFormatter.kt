package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance

import android.content.Context
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.roundTrackDistanceForDisplay

internal final class KilometersTrackDistanceUnitFormatter : ITrackDistanceUnitFormatter {
    public override fun format(context: Context, distanceInMeters: Float): String {
        if (distanceInMeters > KilometersTrackDistanceUnitFormatter.MetersToKilometersFactor) {
            return context.getString(R.string.app_trackdistance_template_kilometers, (distanceInMeters / KilometersTrackDistanceUnitFormatter.MetersToKilometersFactor).roundTrackDistanceForDisplay(context))
        }

        return context.getString(R.string.app_trackdistance_template_meters, distanceInMeters.roundTrackDistanceForDisplay(context))
    }

    companion object {
        private val MetersToKilometersFactor: Float = 1000.0f
    }
}