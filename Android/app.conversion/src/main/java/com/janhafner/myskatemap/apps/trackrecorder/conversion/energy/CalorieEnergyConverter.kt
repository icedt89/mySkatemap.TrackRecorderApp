package com.janhafner.myskatemap.apps.trackrecorder.conversion.energy

public final class CalorieEnergyConverter : IEnergyConverter {
    public override fun convert(burnedEnergyInKilocalories: Float): EnergyConversionResult {
        return EnergyConversionResult(burnedEnergyInKilocalories, EnergyUnit.Kilocalorie)
    }
}