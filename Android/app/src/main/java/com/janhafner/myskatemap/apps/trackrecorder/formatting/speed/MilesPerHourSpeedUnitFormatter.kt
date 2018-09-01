package com.janhafner.myskatemap.apps.trackrecorder.formatting.speed

import com.janhafner.myskatemap.apps.trackrecorder.formatSpeedMilesPerHour

internal final class MilesPerHourSpeedUnitFormatter : ISpeedUnitFormatter {
    public override fun format(speedInMetersPerSecond: Float): String {
        return (speedInMetersPerSecond * MilesPerHourSpeedUnitFormatter.METERS_PER_SECOND_TO_MILES_PER_HOUR_CONVERSION_FACTOR).formatSpeedMilesPerHour()
    }

    companion object {
        private const val METERS_PER_SECOND_TO_MILES_PER_HOUR_CONVERSION_FACTOR: Float = 2.2369f
    }
}