package com.janhafner.myskatemap.apps.trackrecorder.conversion.speed

import com.janhafner.myskatemap.apps.trackrecorder.conversion.speed.SpeedConversionResult

public interface ISpeedConverter {
    fun convert(speedInMetersPerSecond: Float) : SpeedConversionResult
}

