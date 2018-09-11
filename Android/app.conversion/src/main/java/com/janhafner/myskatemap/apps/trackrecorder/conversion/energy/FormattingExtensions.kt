package com.janhafner.myskatemap.apps.trackrecorder.conversion.energy

import com.janhafner.myskatemap.apps.trackrecorder.common.roundWithTwoDecimals

public fun IEnergyConverter.format(value: Float): String {
    return this.convert(value).format()
}

public fun EnergyConversionResult.format() : String {
    return this.value.formatEnergy(this.unit)
}

public fun Float.formatEnergy(unit: EnergyUnit) : String {
    when(unit) {
        EnergyUnit.Kilocalorie ->
            return this.formatBurnedEnergyKilocalorie()
        EnergyUnit.Kilojoule ->
            return this.formatBurnedEnergyKilojoule()
        EnergyUnit.WattHour ->
            return this.formatBurnedEnergyWattHour()
    }
}


public fun Float.formatBurnedEnergyKilocalorie() : String {
    return "${this.roundWithTwoDecimals()} kcal"
}

public fun Float.formatBurnedEnergyKilojoule() : String {
    return "${this.roundWithTwoDecimals()} kJ"
}

public fun Float.formatBurnedEnergyWattHour() : String {
    return "${this.roundWithTwoDecimals()} wH"
}

public fun String.formatBurnedEnergyWattHour() : String {
    return "${this} wH"
}