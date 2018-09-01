package com.janhafner.myskatemap.apps.trackrecorder.formatting.speed

import com.janhafner.myskatemap.apps.trackrecorder.formatSpeedMetersPerSecond

internal final class MetersPerSecondSpeedUnitFormatter : ISpeedUnitFormatter {
    public override fun format(speedInMetersPerSecond: Float): String {
        return speedInMetersPerSecond.formatSpeedMetersPerSecond()
    }
}