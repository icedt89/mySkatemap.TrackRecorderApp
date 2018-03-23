package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance

internal interface ITrackDistanceUnitFormatter {
    fun format(distanceInMeters: Float): String
}

