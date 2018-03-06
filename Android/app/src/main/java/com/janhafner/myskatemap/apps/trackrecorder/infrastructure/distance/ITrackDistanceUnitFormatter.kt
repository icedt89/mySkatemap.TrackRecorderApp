package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance

import android.content.Context

internal interface ITrackDistanceUnitFormatter {
    fun format(context: Context, distanceInMeters: Float): String
}

