package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance

internal interface ITrackDistanceUnitFormatterFactory {
    fun createTrackDistanceUnitFormatter(): ITrackDistanceUnitFormatter
}