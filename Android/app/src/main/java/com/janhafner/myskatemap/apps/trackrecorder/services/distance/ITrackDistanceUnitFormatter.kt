package com.janhafner.myskatemap.apps.trackrecorder.services.distance

internal interface ITrackDistanceUnitFormatter {
    fun format(distanceInMeters: Float): String
}

