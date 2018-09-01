package com.janhafner.myskatemap.apps.trackrecorder.formatting.distance

import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings

internal final class DistanceUnitFormatterFactory(private val appSettings: IAppSettings): IDistanceUnitFormatterFactory {
    private val milesDistanceUnitFormatter: IDistanceUnitFormatter = MilesDistanceUnitFormatter()

    private val kilometersDistanceUnitFormatter: IDistanceUnitFormatter = KilometersDistanceUnitFormatter()

    public override fun createFormatter(): IDistanceUnitFormatter {
        if(this.appSettings.distanceUnitFormatterTypeName == MilesDistanceUnitFormatter::class.java.name) {
            return this.milesDistanceUnitFormatter
        }

        return this.kilometersDistanceUnitFormatter
    }
}