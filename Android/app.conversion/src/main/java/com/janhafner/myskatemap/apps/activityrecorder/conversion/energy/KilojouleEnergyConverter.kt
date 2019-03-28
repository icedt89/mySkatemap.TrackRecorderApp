package com.janhafner.myskatemap.apps.activityrecorder.conversion.energy

public final class KilojouleEnergyConverter : IEnergyConverter {
    public override fun convert(burnedEnergyInKilocalories: Float): EnergyConversionResult {
        return EnergyConversionResult(burnedEnergyInKilocalories * KILO_CALORIES_TO_KILO_JOULE_CONVERSION_FACTOR, EnergyUnit.Kilojoule)
    }

    companion object {
        private const val KILO_CALORIES_TO_KILO_JOULE_CONVERSION_FACTOR: Float = 4.1868f
    }
}