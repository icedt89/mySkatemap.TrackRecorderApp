package com.janhafner.myskatemap.apps.trackrecorder.formatting.speed

import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings

internal final class SpeedUnitFormatterFactory(private val appSettings: IAppSettings) : ISpeedUnitFormatterFactory {
    private val metersPerSecondSpeedUnitFormatter: ISpeedUnitFormatter = MetersPerSecondSpeedUnitFormatter()

    private val kilometersPerHourSpeedUnitFormatter: ISpeedUnitFormatter = KilometersPerHourSpeedUnitFormatter()

    private val milesPerHourSpeedUnitFormatter: ISpeedUnitFormatter = MilesPerHourSpeedUnitFormatter()

    public override fun createFormatter(): ISpeedUnitFormatter {
        if(this.appSettings.speedUnitFormatterTypeName == MetersPerSecondSpeedUnitFormatter::class.java.name) {
            return this.metersPerSecondSpeedUnitFormatter
        }

        if(this.appSettings.speedUnitFormatterTypeName == MilesPerHourSpeedUnitFormatter::class.java.name) {
            return this.milesPerHourSpeedUnitFormatter
        }

        return this.kilometersPerHourSpeedUnitFormatter
    }
}