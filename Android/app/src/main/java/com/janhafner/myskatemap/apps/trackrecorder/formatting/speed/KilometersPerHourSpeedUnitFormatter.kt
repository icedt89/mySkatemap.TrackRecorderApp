package com.janhafner.myskatemap.apps.trackrecorder.formatting.speed

import com.janhafner.myskatemap.apps.trackrecorder.formatSpeedKilometersPerHour

internal final class KilometersPerHourSpeedUnitFormatter : ISpeedUnitFormatter {
    public override fun format(speedInMetersPerSecond: Float): String {
        return (speedInMetersPerSecond * KilometersPerHourSpeedUnitFormatter.METERS_PER_SECOND_TO_KILOMETERS_PER_HOUR_CONVERSION_FACTOR).formatSpeedKilometersPerHour()
    }

    companion object {
        private const val METERS_PER_SECOND_TO_KILOMETERS_PER_HOUR_CONVERSION_FACTOR: Float = 3.6f
    }
}