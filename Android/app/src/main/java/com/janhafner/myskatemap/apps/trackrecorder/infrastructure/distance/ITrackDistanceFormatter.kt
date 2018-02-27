package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance

import android.content.Context

internal interface ITrackDistanceFormatter {
    fun format(context: Context, distanceInMeters: Float): String
}

