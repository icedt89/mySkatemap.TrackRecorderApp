package com.janhafner.myskatemap.apps.trackrecorder.formatting.distance

internal interface IDistanceUnitFormatter {
    fun format(distanceInMeters: Float): String
}

