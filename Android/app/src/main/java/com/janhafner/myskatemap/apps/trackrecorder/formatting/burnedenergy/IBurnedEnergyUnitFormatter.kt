package com.janhafner.myskatemap.apps.trackrecorder.formatting.burnedenergy

internal interface IBurnedEnergyUnitFormatter {
    fun format(burnedEnergyInKilocalories: Float) : String
}

