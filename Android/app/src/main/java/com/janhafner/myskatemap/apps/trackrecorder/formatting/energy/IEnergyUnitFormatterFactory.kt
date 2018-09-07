package com.janhafner.myskatemap.apps.trackrecorder.formatting.energy

internal interface IEnergyUnitFormatterFactory {
    fun createFormatter() : IEnergyUnitFormatter
}