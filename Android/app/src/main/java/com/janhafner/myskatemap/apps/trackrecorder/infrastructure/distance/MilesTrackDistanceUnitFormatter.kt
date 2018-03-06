package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance

import android.content.Context
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.roundTrackDistanceForDisplay

internal final class MilesTrackDistanceUnitFormatter : ITrackDistanceUnitFormatter {
    public override fun format(context: Context, distanceInMeters: Float): String {
        if (distanceInMeters > MilesTrackDistanceUnitFormatter.MetersToMiles) {
            return context.getString(R.string.app_trackdistance_template_kilometers, (distanceInMeters / MilesTrackDistanceUnitFormatter.MetersToMiles).roundTrackDistanceForDisplay(context))
        }

        return context.getString(R.string.app_trackdistance_template_miles, distanceInMeters.roundTrackDistanceForDisplay(context))
    }

    companion object {
        private val MetersToMiles: Float = 1609.344f
    }
}