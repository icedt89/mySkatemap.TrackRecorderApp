package com.janhafner.myskatemap.apps.trackrecorder.formatting.energy

internal interface IEnergyUnitFormatter {
    fun format(burnedEnergyInKilocalories: Float) : String
}

