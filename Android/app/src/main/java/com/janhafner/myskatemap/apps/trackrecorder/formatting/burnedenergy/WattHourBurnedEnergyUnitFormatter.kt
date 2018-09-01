package com.janhafner.myskatemap.apps.trackrecorder.formatting.burnedenergy

import com.janhafner.myskatemap.apps.trackrecorder.formatBurnedEnergyWattHour

internal final class WattHourBurnedEnergyUnitFormatter : IBurnedEnergyUnitFormatter {
    public override fun format(burnedEnergyInKilocalories: Float): String {
        return (burnedEnergyInKilocalories * WattHourBurnedEnergyUnitFormatter.KILO_CALORIES_TO_WATT_HOUR_CONVERSION_FACTOR).formatBurnedEnergyWattHour()
    }

    companion object {
        private const val KILO_CALORIES_TO_WATT_HOUR_CONVERSION_FACTOR: Float = 1.163f
    }
}