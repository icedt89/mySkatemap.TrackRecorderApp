package com.janhafner.myskatemap.apps.activityrecorder.conversion.energy

public final class KilocalorieEnergyConverter : IEnergyConverter {
    public override fun convert(burnedEnergyInKilocalories: Float): EnergyConversionResult {
        return EnergyConversionResult(burnedEnergyInKilocalories, EnergyUnit.Kilocalorie)
    }
}