package com.janhafner.myskatemap.apps.trackrecorder.services.distance

import android.content.Context
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.roundTrackDistanceForDisplay

internal final class MilesTrackDistanceUnitFormatter(private val context: Context) : ITrackDistanceUnitFormatter {
    public override fun format(distanceInMeters: Float): String {
        if (distanceInMeters > MilesTrackDistanceUnitFormatter.MetersToMiles) {
            return this.context.getString(R.string.app_trackdistance_template_miles, (distanceInMeters / MilesTrackDistanceUnitFormatter.MetersToMiles).roundTrackDistanceForDisplay(this.context))
        }

        return this.context.getString(R.string.app_trackdistance_template_meters, distanceInMeters.roundTrackDistanceForDisplay(this.context))
    }

    companion object {
        private const val MetersToMiles: Float = 1609.344f
    }
}