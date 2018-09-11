package com.janhafner.myskatemap.apps.trackrecorder.conversion.speed

import com.janhafner.myskatemap.apps.trackrecorder.common.roundWithTwoDecimals

public fun ISpeedConverter.format(value: Float): String {
    return this.convert(value).format()
}

public fun SpeedConversionResult.format() : String {
    return this.value.formatSpeed(this.unit)
}

public fun Float.formatSpeed(unit: SpeedUnit) : String {
    when(unit) {
        SpeedUnit.MetersPerSecond ->
            return this.formatSpeedMetersPerSecond()
        SpeedUnit.KilometersPerHour ->
            return this.formatSpeedKilometersPerHour()
        SpeedUnit.MilesPerHour ->
            return this.formatSpeedMilesPerHour()
    }
}

public fun Float.formatSpeedMetersPerSecond() : String {
    return "${this.roundWithTwoDecimals()} m/s"
}

public fun Float.formatSpeedKilometersPerHour() : String {
    return "${this.roundWithTwoDecimals()} km/h"
}

public fun Float.formatSpeedMilesPerHour() : String {
    return "${this.roundWithTwoDecimals()} mi/h"
}