package com.janhafner.myskatemap.apps.activityrecorder.infrastructure.distance

import com.janhafner.myskatemap.apps.activityrecorder.conversion.distance.IDistanceConverter
import com.janhafner.myskatemap.apps.activityrecorder.conversion.distance.IDistanceConverterFactory
import com.janhafner.myskatemap.apps.activityrecorder.conversion.distance.KilometersDistanceConverter
import com.janhafner.myskatemap.apps.activityrecorder.conversion.distance.MilesDistanceConverter
import com.janhafner.myskatemap.apps.activityrecorder.settings.IAppSettings

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