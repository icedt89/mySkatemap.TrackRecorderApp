package com.janhafner.myskatemap.apps.trackrecorder.formatting.burnedenergy

internal interface IBurnedEnergyUnitFormatterFactory {
    fun createFormatter() : IBurnedEnergyUnitFormatter
}