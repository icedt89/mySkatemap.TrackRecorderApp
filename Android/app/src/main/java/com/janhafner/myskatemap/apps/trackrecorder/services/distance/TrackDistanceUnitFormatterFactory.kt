package com.janhafner.myskatemap.apps.trackrecorder.services.distance

import android.content.Context
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings

internal final class TrackDistanceUnitFormatterFactory(private val appSettings: IAppSettings, private val context: Context): ITrackDistanceUnitFormatterFactory {
    private val milesTrackDistanceUnitFormatter: ITrackDistanceUnitFormatter = MilesTrackDistanceUnitFormatter(this.context)

    private val kilometersTrackDistanceUnitFormatter: ITrackDistanceUnitFormatter = KilometersTrackDistanceUnitFormatter(this.context)

    public override fun createTrackDistanceUnitFormatter(): ITrackDistanceUnitFormatter {
        if(this.appSettings.trackDistanceUnitFormatterTypeName == MilesTrackDistanceUnitFormatter::class.java.name) {
            return this.milesTrackDistanceUnitFormatter
        }

        return this.kilometersTrackDistanceUnitFormatter
    }
}