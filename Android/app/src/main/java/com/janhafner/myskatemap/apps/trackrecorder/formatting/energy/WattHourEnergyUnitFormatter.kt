package com.janhafner.myskatemap.apps.trackrecorder.formatting.energy

import com.janhafner.myskatemap.apps.trackrecorder.formatBurnedEnergyWattHour

internal final class WattHourEnergyUnitFormatter : IEnergyUnitFormatter {
    public override fun format(burnedEnergyInKilocalories: Float): String {
        return (burnedEnergyInKilocalories * WattHourEnergyUnitFormatter.KILO_CALORIES_TO_WATT_HOUR_CONVERSION_FACTOR).formatBurnedEnergyWattHour()
    }

    companion object {
        private const val KILO_CALORIES_TO_WATT_HOUR_CONVERSION_FACTOR: Float = 1.163f
    }
}