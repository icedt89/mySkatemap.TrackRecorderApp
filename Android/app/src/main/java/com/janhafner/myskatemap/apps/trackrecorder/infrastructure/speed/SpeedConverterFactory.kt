package com.janhafner.myskatemap.apps.trackrecorder.formatting.speed

import com.janhafner.myskatemap.apps.trackrecorder.conversion.speed.ISpeedConverter
import com.janhafner.myskatemap.apps.trackrecorder.conversion.speed.KilometersPerHourSpeedConverter
import com.janhafner.myskatemap.apps.trackrecorder.conversion.speed.MetersPerSecondSpeedConverter
import com.janhafner.myskatemap.apps.trackrecorder.conversion.speed.MilesPerHourSpeedConverter
import com.janhafner.myskatemap.apps.trackrecorder.conversion.speed.ISpeedConverterFactory
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings

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