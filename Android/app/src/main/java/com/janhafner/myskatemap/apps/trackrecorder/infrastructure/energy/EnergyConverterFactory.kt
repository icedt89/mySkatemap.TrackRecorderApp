package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.energy

import com.janhafner.myskatemap.apps.trackrecorder.conversion.energy.IEnergyConverter
import com.janhafner.myskatemap.apps.trackrecorder.conversion.energy.KilocalorieEnergyConverter
import com.janhafner.myskatemap.apps.trackrecorder.conversion.energy.KilojouleEnergyConverter
import com.janhafner.myskatemap.apps.trackrecorder.conversion.energy.WattHourEnergyConverter
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings

internal final class EnergyConverterFactory(private val appSettings: IAppSettings) : IEnergyConverterFactory {
    private val kilocalorieEnergyUnitFormatter: IEnergyConverter = KilocalorieEnergyConverter()

    private val kilojoulEnergyUnitFormatter: IEnergyConverter = KilojouleEnergyConverter()

    private val wattHourEnergyUnitFormatter: IEnergyConverter = WattHourEnergyConverter()

    public override fun createConverter(): IEnergyConverter {
        if(this.appSettings.energyUnitFormatterTypeName == KilojouleEnergyConverter::class.java.simpleName) {
            return this.kilojoulEnergyUnitFormatter
        }

        if(this.appSettings.energyUnitFormatterTypeName == WattHourEnergyConverter::class.java.simpleName) {
            return this.wattHourEnergyUnitFormatter
        }

        return this.kilocalorieEnergyUnitFormatter
    }
}