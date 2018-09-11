package com.janhafner.myskatemap.apps.trackrecorder.conversion.energy

public final class WattHourEnergyConverter : IEnergyConverter {
    public override fun convert(burnedEnergyInKilocalories: Float): EnergyConversionResult {
        return EnergyConversionResult(burnedEnergyInKilocalories * KILO_CALORIES_TO_WATT_HOUR_CONVERSION_FACTOR, EnergyUnit.WattHour)
    }

    companion object {
        private const val KILO_CALORIES_TO_WATT_HOUR_CONVERSION_FACTOR: Float = 1.163f
    }
}