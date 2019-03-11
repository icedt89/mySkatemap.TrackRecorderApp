package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.energy

import com.janhafner.myskatemap.apps.trackrecorder.core.roundWithTwoDecimalsAndFormatWithUnit
import com.janhafner.myskatemap.apps.trackrecorder.conversion.energy.EnergyUnit
import com.janhafner.myskatemap.apps.trackrecorder.conversion.energy.IEnergyConverter

public fun IEnergyConverter.format(value: Float): String {
    val conversionResult = this.convert(value)

    when(conversionResult.unit) {
        EnergyUnit.Kilocalorie ->
            return conversionResult.value.roundWithTwoDecimalsAndFormatWithUnit(SYMBOL_ENERGY_KILOCALORIE)
        EnergyUnit.Kilojoule ->
            return conversionResult.value.roundWithTwoDecimalsAndFormatWithUnit(SYMBOL_ENERGY_KILOJOULE)
        EnergyUnit.WattHour ->
            return conversionResult.value.roundWithTwoDecimalsAndFormatWithUnit(SYMBOL_ENERGY_WATT_HOUR)
    }
}

public fun EnergyUnit.getUnitSymbol() : String {
    when(this) {
        EnergyUnit.Kilocalorie ->
            return SYMBOL_ENERGY_KILOCALORIE
        EnergyUnit.Kilojoule ->
            return SYMBOL_ENERGY_KILOJOULE
        EnergyUnit.WattHour ->
            return SYMBOL_ENERGY_WATT_HOUR
    }
}

public const val SYMBOL_ENERGY_KILOCALORIE: String = "kcal"

public const val SYMBOL_ENERGY_KILOJOULE: String = "kJ"

public const val SYMBOL_ENERGY_WATT_HOUR: String = "wH"