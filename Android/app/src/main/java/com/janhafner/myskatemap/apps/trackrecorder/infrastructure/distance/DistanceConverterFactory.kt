package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance

import com.janhafner.myskatemap.apps.trackrecorder.conversion.distance.IDistanceConverter
import com.janhafner.myskatemap.apps.trackrecorder.conversion.distance.IDistanceConverterFactory
import com.janhafner.myskatemap.apps.trackrecorder.conversion.distance.KilometersDistanceConverter
import com.janhafner.myskatemap.apps.trackrecorder.conversion.distance.MilesDistanceConverter
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings

internal final class DistanceConverterFactory(private val appSettings: IAppSettings): IDistanceConverterFactory {
    private val milesDistanceUnitFormatter: IDistanceConverter = MilesDistanceConverter()

    private val kilometersDistanceUnitFormatter: IDistanceConverter = KilometersDistanceConverter()

    public override fun createConverter(): IDistanceConverter {
        if(this.appSettings.distanceConverterTypeName == MilesDistanceConverter::class.java.simpleName) {
            return this.milesDistanceUnitFormatter
        }

        return this.kilometersDistanceUnitFormatter
    }
}