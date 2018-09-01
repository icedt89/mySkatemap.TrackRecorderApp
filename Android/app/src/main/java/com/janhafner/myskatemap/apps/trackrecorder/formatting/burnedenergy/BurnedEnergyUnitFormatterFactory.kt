package com.janhafner.myskatemap.apps.trackrecorder.formatting.burnedenergy

import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings

internal final class BurnedEnergyUnitFormatterFactory(private val appSettings: IAppSettings) : IBurnedEnergyUnitFormatterFactory {
    private val kilocalorieBurnedEnergyUnitFormatter: IBurnedEnergyUnitFormatter = KilocalorieBurnedEnergyUnitFormatter()

    private val kilojoulBurnedEnergyUnitFormatter: IBurnedEnergyUnitFormatter = KilojouleBurnedEnergyUnitFormatter()

    private val wattHourBurnedEnergyUnitFormatter: IBurnedEnergyUnitFormatter = WattHourBurnedEnergyUnitFormatter()

    public override fun createFormatter(): IBurnedEnergyUnitFormatter {
        if(this.appSettings.burnedEnergyUnitFormatterTypeName == KilojouleBurnedEnergyUnitFormatter::class.java.name) {
            return this.kilojoulBurnedEnergyUnitFormatter
        }

        if(this.appSettings.burnedEnergyUnitFormatterTypeName == WattHourBurnedEnergyUnitFormatter::class.java.name) {
            return this.wattHourBurnedEnergyUnitFormatter
        }

        return this.kilocalorieBurnedEnergyUnitFormatter
    }
}