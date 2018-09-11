package com.janhafner.myskatemap.apps.trackrecorder.conversion.energy

import com.janhafner.myskatemap.apps.trackrecorder.conversion.energy.EnergyConversionResult

public interface IEnergyConverter {
    fun convert(burnedEnergyInKilocalories: Float) : EnergyConversionResult
}

