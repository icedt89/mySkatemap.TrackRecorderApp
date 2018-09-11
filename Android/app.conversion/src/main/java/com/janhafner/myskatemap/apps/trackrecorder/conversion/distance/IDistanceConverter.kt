package com.janhafner.myskatemap.apps.trackrecorder.conversion.distance

public interface IDistanceConverter {
    fun convert(distanceInMeters: Float) : DistanceConversionResult
}

