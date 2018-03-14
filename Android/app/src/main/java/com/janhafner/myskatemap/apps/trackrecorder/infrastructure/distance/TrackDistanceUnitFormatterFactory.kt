package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance

import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.settings.IAppSettings

internal final class TrackDistanceUnitFormatterFactory(private val appSettings: IAppSettings): ITrackDistanceUnitFormatterFactory {
    private val milesTrackDistanceUnitFormatter: ITrackDistanceUnitFormatter = MilesTrackDistanceUnitFormatter()

    private val kilometersTrackDistanceUnitFormatter: ITrackDistanceUnitFormatter = KilometersTrackDistanceUnitFormatter()

    public override fun createTrackDistanceUnitFormatter(): ITrackDistanceUnitFormatter {
        if(this.appSettings.trackDistanceUnitFormatterTypeName == MilesTrackDistanceUnitFormatter::class.java.name) {
            return this.milesTrackDistanceUnitFormatter
        }

        return this.kilometersTrackDistanceUnitFormatter
    }
}