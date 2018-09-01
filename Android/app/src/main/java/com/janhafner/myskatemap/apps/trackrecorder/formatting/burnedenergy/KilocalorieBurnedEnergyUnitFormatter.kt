package com.janhafner.myskatemap.apps.trackrecorder.formatting.burnedenergy

import com.janhafner.myskatemap.apps.trackrecorder.formatBurnedEnergyKilocalorie

internal final class KilocalorieBurnedEnergyUnitFormatter : IBurnedEnergyUnitFormatter {
    public override fun format(burnedEnergyInKilocalories: Float): String {
        return burnedEnergyInKilocalories.formatBurnedEnergyKilocalorie()
    }
}