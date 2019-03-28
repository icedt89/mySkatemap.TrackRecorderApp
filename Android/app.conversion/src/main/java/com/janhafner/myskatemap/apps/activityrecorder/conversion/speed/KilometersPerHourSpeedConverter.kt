package com.janhafner.myskatemap.apps.activityrecorder.conversion.speed

public final class KilometersPerHourSpeedConverter : ISpeedConverter {
    public override fun convert(speedInMetersPerSecond: Float): SpeedConversionResult {
        return SpeedConversionResult(speedInMetersPerSecond * METERS_PER_SECOND_TO_KILOMETERS_PER_HOUR_CONVERSION_FACTOR, SpeedUnit.KilometersPerHour)
    }

    public companion object {
        private const val METERS_PER_SECOND_TO_KILOMETERS_PER_HOUR_CONVERSION_FACTOR: Float = 3.6f
    }
}