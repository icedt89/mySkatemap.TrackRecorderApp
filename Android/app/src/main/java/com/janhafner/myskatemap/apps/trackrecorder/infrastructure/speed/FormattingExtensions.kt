package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.speed

import com.janhafner.myskatemap.apps.trackrecorder.common.roundWithTwoDecimalsAndFormatWithUnit
import com.janhafner.myskatemap.apps.trackrecorder.conversion.speed.ISpeedConverter
import com.janhafner.myskatemap.apps.trackrecorder.conversion.speed.SpeedUnit

public fun ISpeedConverter.format(value: Float): String {
    val conversionResult = this.convert(value)

    when(conversionResult.unit) {
        SpeedUnit.MetersPerSecond ->
            return conversionResult.value.roundWithTwoDecimalsAndFormatWithUnit(SYMBOL_SPEED_METERS_PER_SECOND)
        SpeedUnit.KilometersPerHour ->
            return conversionResult.value.roundWithTwoDecimalsAndFormatWithUnit(SYMBOL_SPEED_KILOMETERS_PER_HOUR)
        SpeedUnit.MilesPerHour ->
            return conversionResult.value.roundWithTwoDecimalsAndFormatWithUnit(SYMBOL_SPEED_MILES_PER_HOUR)
    }
}

public fun SpeedUnit.getUnitSymbol() : String {
    when(this) {
        SpeedUnit.MetersPerSecond ->
            return SYMBOL_SPEED_METERS_PER_SECOND
        SpeedUnit.KilometersPerHour ->
            return SYMBOL_SPEED_KILOMETERS_PER_HOUR
        SpeedUnit.MilesPerHour ->
            return SYMBOL_SPEED_MILES_PER_HOUR
    }
}

public const val SYMBOL_SPEED_MILES_PER_HOUR: String ="mi/h"

public const val SYMBOL_SPEED_KILOMETERS_PER_HOUR: String ="km/h"

public const val SYMBOL_SPEED_METERS_PER_SECOND: String ="m/s"