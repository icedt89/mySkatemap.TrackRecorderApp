package com.janhafner.myskatemap.apps.trackrecorder.conversion.energy

import com.janhafner.myskatemap.apps.trackrecorder.conversion.energy.EnergyConversionResult
import com.janhafner.myskatemap.apps.trackrecorder.conversion.energy.EnergyUnit
import com.janhafner.myskatemap.apps.trackrecorder.conversion.energy.IEnergyConverter

public final class KilocalorieEnergyConverter : IEnergyConverter {
    public override fun convert(burnedEnergyInKilocalories: Float): EnergyConversionResult {
        return EnergyConversionResult(burnedEnergyInKilocalories, EnergyUnit.Kilocalorie)
    }
}