package com.janhafner.myskatemap.apps.trackrecorder.formatting.energy

import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings

internal final class EnergyUnitFormatterFactory(private val appSettings: IAppSettings) : IEnergyUnitFormatterFactory {
    private val kilocalorieEnergyUnitFormatter: IEnergyUnitFormatter = KilocalorieEnergyUnitFormatter()

    private val kilojoulEnergyUnitFormatter: IEnergyUnitFormatter = KilojouleEnergyUnitFormatter()

    private val wattHourEnergyUnitFormatter: IEnergyUnitFormatter = WattHourEnergyUnitFormatter()

    public override fun createFormatter(): IEnergyUnitFormatter {
        if(this.appSettings.energyUnitFormatterTypeName == KilojouleEnergyUnitFormatter::class.java.simpleName) {
            return this.kilojoulEnergyUnitFormatter
        }

        if(this.appSettings.energyUnitFormatterTypeName == WattHourEnergyUnitFormatter::class.java.simpleName) {
            return this.wattHourEnergyUnitFormatter
        }

        return this.kilocalorieEnergyUnitFormatter
    }
}