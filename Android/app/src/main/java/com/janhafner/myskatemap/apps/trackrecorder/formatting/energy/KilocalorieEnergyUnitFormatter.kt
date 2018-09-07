package com.janhafner.myskatemap.apps.trackrecorder.formatting.energy

import com.janhafner.myskatemap.apps.trackrecorder.formatBurnedEnergyKilocalorie

internal final class KilocalorieEnergyUnitFormatter : IEnergyUnitFormatter {
    public override fun format(burnedEnergyInKilocalories: Float): String {
        return burnedEnergyInKilocalories.formatBurnedEnergyKilocalorie()
    }
}