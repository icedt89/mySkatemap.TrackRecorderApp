package com.janhafner.myskatemap.apps.activityrecorder.conversion.distance

public final class MilesDistanceConverter : IDistanceConverter {
    public override fun convert(distanceInMeters: Float): DistanceConversionResult {
        if (distanceInMeters > METERS_TO_MILES_CONVERSION_FACTOR) {
            return DistanceConversionResult(distanceInMeters / METERS_TO_MILES_CONVERSION_FACTOR, DistanceUnit.Miles)
        }

        return DistanceConversionResult(distanceInMeters, DistanceUnit.Meters)
    }

    public companion object {
        private const val METERS_TO_MILES_CONVERSION_FACTOR: Float = 1609.344f
    }
}