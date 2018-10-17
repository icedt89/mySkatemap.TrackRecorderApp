package com.janhafner.myskatemap.apps.trackrecorder.conversion.speed

public final class MetersPerSecondSpeedConverter : ISpeedConverter {
    public override fun convert(speedInMetersPerSecond: Float): SpeedConversionResult {
        return SpeedConversionResult(speedInMetersPerSecond, SpeedUnit.MetersPerSecond)
    }
}