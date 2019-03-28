package com.janhafner.myskatemap.apps.activityrecorder.conversion.distance

public final class KilometersDistanceConverter : IDistanceConverter {
    public override fun convert(distanceInMeters: Float): DistanceConversionResult {
        if (distanceInMeters > METERS_TO_KILOMETERS_CONVERSION_FACTOR) {
            return DistanceConversionResult(distanceInMeters / METERS_TO_KILOMETERS_CONVERSION_FACTOR, DistanceUnit.Kilometers)
        }

        return DistanceConversionResult(distanceInMeters, DistanceUnit.Meters)
    }

    public companion object {
        private const val METERS_TO_KILOMETERS_CONVERSION_FACTOR: Float = 1000.0f
    }
}