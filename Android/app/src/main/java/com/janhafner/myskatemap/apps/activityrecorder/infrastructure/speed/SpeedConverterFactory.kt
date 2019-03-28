package com.janhafner.myskatemap.apps.activityrecorder.formatting.speed

import com.janhafner.myskatemap.apps.activityrecorder.conversion.speed.ISpeedConverter
import com.janhafner.myskatemap.apps.activityrecorder.conversion.speed.KilometersPerHourSpeedConverter
import com.janhafner.myskatemap.apps.activityrecorder.conversion.speed.MetersPerSecondSpeedConverter
import com.janhafner.myskatemap.apps.activityrecorder.conversion.speed.MilesPerHourSpeedConverter
import com.janhafner.myskatemap.apps.activityrecorder.conversion.speed.ISpeedConverterFactory
import com.janhafner.myskatemap.apps.activityrecorder.settings.IAppSettings

internal final class SpeedConverterFactory(private val appSettings: IAppSettings) : ISpeedConverterFactory {
    private val metersPerSecondSpeedUnitFormatter: ISpeedConverter = MetersPerSecondSpeedConverter()

    private val kilometersPerHourSpeedUnitFormatter: ISpeedConverter = KilometersPerHourSpeedConverter()

    private val milesPerHourSpeedUnitFormatter: ISpeedConverter = MilesPerHourSpeedConverter()

    public override fun createConverter(): ISpeedConverter {
        if(this.appSettings.speedConverterTypeName == MetersPerSecondSpeedConverter::class.java.simpleName) {
            return this.metersPerSecondSpeedUnitFormatter
        }

        if(this.appSettings.speedConverterTypeName == MilesPerHourSpeedConverter::class.java.simpleName) {
            return this.milesPerHourSpeedUnitFormatter
        }

        return this.kilometersPerHourSpeedUnitFormatter
    }
}