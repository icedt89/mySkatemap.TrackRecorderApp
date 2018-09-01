package com.janhafner.myskatemap.apps.trackrecorder.formatting.speed

internal interface ISpeedUnitFormatter {
    fun format(speedInMetersPerSecond: Float): String
}

