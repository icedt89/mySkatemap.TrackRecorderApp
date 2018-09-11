package com.janhafner.myskatemap.apps.trackrecorder.conversion.speed

import com.janhafner.myskatemap.apps.trackrecorder.conversion.speed.ISpeedConverter
import com.janhafner.myskatemap.apps.trackrecorder.conversion.speed.SpeedConversionResult
import com.janhafner.myskatemap.apps.trackrecorder.conversion.speed.SpeedUnit

public final class MetersPerSecondSpeedConverter : ISpeedConverter {
    public override fun convert(speedInMetersPerSecond: Float): SpeedConversionResult {
        return SpeedConversionResult(speedInMetersPerSecond, SpeedUnit.MetersPerSecond)
    }
}