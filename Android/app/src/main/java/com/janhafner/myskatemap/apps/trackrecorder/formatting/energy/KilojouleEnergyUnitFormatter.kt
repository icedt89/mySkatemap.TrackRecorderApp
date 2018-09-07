package com.janhafner.myskatemap.apps.trackrecorder.formatting.energy

import com.janhafner.myskatemap.apps.trackrecorder.formatBurnedEnergyKilojoule

internal final class KilojouleEnergyUnitFormatter : IEnergyUnitFormatter {
    public override fun format(burnedEnergyInKilocalories: Float): String {
        return (burnedEnergyInKilocalories * KilojouleEnergyUnitFormatter.KILO_CALORIES_TO_KILO_JOULE_CONVERSION_FACTOR).formatBurnedEnergyKilojoule()
    }

    companion object {
        private const val KILO_CALORIES_TO_KILO_JOULE_CONVERSION_FACTOR: Float = 4.1868f
    }
}