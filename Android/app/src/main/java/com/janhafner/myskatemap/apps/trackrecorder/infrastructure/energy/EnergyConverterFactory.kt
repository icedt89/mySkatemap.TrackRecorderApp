package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.energy

import com.janhafner.myskatemap.apps.trackrecorder.conversion.energy.*
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings

internal final class EnergyConverterFactory(private val appSettings: IAppSettings) : IEnergyConverterFactory {
    private val kilocalorieEnergyUnitFormatter: IEnergyConverter = KilocalorieEnergyConverter()

    private val kilojoulEnergyUnitFormatter: IEnergyConverter = KilojouleEnergyConverter()

    private val wattHourEnergyUnitFormatter: IEnergyConverter = WattHourEnergyConverter()

    public override fun createConverter(): IEnergyConverter {
        if(this.appSettings.energyConverterTypeName == KilojouleEnergyConverter::class.java.simpleName) {
            return this.kilojoulEnergyUnitFormatter
        }

        if(this.appSettings.energyConverterTypeName == WattHourEnergyConverter::class.java.simpleName) {
            return this.wattHourEnergyUnitFormatter
        }

        return this.kilocalorieEnergyUnitFormatter
    }
}