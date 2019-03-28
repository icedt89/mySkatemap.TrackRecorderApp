package com.janhafner.myskatemap.apps.activityrecorder.infrastructure.energy

import com.janhafner.myskatemap.apps.activityrecorder.conversion.energy.*
import com.janhafner.myskatemap.apps.activityrecorder.settings.IAppSettings

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