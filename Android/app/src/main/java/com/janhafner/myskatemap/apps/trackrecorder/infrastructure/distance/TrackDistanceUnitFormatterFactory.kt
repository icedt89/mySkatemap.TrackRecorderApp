package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance

import android.util.Log
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.settings.IAppSettings

internal final class TrackDistanceUnitFormatterFactory(private val appSettings: IAppSettings): ITrackDistanceUnitFormatterFactory {
    private val logTag: String = this.javaClass.simpleName

    private val milesTrackDistanceUnitFormatter: ITrackDistanceUnitFormatter = MilesTrackDistanceUnitFormatter()

    private val kilometersTrackDistanceUnitFormatter: ITrackDistanceUnitFormatter = KilometersTrackDistanceUnitFormatter()

    public override fun createTrackDistanceUnitFormatter(): ITrackDistanceUnitFormatter {
        if(this.appSettings.trackDistanceUnitFormatterTypeName == MilesTrackDistanceUnitFormatter::javaClass.name) {
            return this.milesTrackDistanceUnitFormatter
        }

        if(this.appSettings.trackDistanceUnitFormatterTypeName != KilometersTrackDistanceUnitFormatter::javaClass.name) {
            Log.wtf(this.logTag, "Not a valid TrackDistanceUnitFormatter implementation: \"${this.appSettings.trackDistanceUnitFormatterTypeName}\"")
        }

        return this.kilometersTrackDistanceUnitFormatter
    }
}