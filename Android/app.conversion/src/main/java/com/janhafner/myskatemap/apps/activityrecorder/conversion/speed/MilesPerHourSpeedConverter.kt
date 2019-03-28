package com.janhafner.myskatemap.apps.activityrecorder.conversion.speed

public final class MilesPerHourSpeedConverter : ISpeedConverter {
    public override fun convert(speedInMetersPerSecond: Float): SpeedConversionResult {
        return SpeedConversionResult(speedInMetersPerSecond * METERS_PER_SECOND_TO_MILES_PER_HOUR_CONVERSION_FACTOR, SpeedUnit.MilesPerHour)
    }

    public companion object {
        private const val METERS_PER_SECOND_TO_MILES_PER_HOUR_CONVERSION_FACTOR: Float = 2.2369f
    }
}