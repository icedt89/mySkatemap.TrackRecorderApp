package com.janhafner.myskatemap.apps.trackrecorder.formatting.distance

internal interface IDistanceUnitFormatterFactory {
    fun createFormatter(): IDistanceUnitFormatter
}