package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance

import com.janhafner.myskatemap.apps.trackrecorder.core.roundWithTwoDecimalsAndFormatWithUnit
import com.janhafner.myskatemap.apps.trackrecorder.conversion.distance.DistanceUnit
import com.janhafner.myskatemap.apps.trackrecorder.conversion.distance.IDistanceConverter

public fun IDistanceConverter.format(value: Float): String {
    val conversionResult = this.convert(value)

    when(conversionResult.unit) {
        DistanceUnit.Kilometers ->
            return conversionResult.value.roundWithTwoDecimalsAndFormatWithUnit(SYMBOL_DISTANCE_KILOMETERS)
        DistanceUnit.Meters ->
            return conversionResult.value.roundWithTwoDecimalsAndFormatWithUnit(SYMBOL_DISTANCE_METERS)
        DistanceUnit.Miles ->
            return conversionResult.value.roundWithTwoDecimalsAndFormatWithUnit(SYMBOL_DISTANCE_MILES)
    }
}

public fun DistanceUnit.getUnitSymbol() : String {
    when(this) {
        DistanceUnit.Kilometers ->
            return SYMBOL_DISTANCE_KILOMETERS
        DistanceUnit.Meters ->
            return SYMBOL_DISTANCE_METERS
        DistanceUnit.Miles ->
            return SYMBOL_DISTANCE_MILES
    }
}

public const val SYMBOL_DISTANCE_KILOMETERS: String = "km"

public const val SYMBOL_DISTANCE_METERS: String = "m"

public const val SYMBOL_DISTANCE_MILES: String = "mi"