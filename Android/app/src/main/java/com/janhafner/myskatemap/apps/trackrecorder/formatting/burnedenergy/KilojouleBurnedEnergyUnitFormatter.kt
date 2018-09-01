package com.janhafner.myskatemap.apps.trackrecorder.formatting.burnedenergy

import com.janhafner.myskatemap.apps.trackrecorder.formatBurnedEnergyKilojoule

internal final class KilojouleBurnedEnergyUnitFormatter : IBurnedEnergyUnitFormatter {
    public override fun format(burnedEnergyInKilocalories: Float): String {
        return (burnedEnergyInKilocalories * KilojouleBurnedEnergyUnitFormatter.KILO_CALORIES_TO_KILO_JOULE_CONVERSION_FACTOR).formatBurnedEnergyKilojoule()
    }

    companion object {
        private const val KILO_CALORIES_TO_KILO_JOULE_CONVERSION_FACTOR: Float = 4.1868f
    }
}