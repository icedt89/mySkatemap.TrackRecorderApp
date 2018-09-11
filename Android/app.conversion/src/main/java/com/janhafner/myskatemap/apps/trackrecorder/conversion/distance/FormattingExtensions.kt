package com.janhafner.myskatemap.apps.trackrecorder.conversion.distance

import com.janhafner.myskatemap.apps.trackrecorder.common.roundWithTwoDecimals

public fun IDistanceConverter.format(value: Float): String {
    return this.convert(value).format()
}

public fun DistanceConversionResult.format() : String {
    return this.value.formatDistance(this.unit)
}

public fun Float.formatDistance(unit: DistanceUnit) : String {
    when(unit) {
        DistanceUnit.Meters ->
            return this.formatDistanceMeters()
        DistanceUnit.Kilometers ->
            return this.formatDistanceKilometers()
        DistanceUnit.Miles ->
            return this.formatDistanceMiles()
    }
}

public fun Float.formatDistanceKilometers() : String {
    return "${this.roundWithTwoDecimals()} km"
}

public fun Float.formatDistanceMiles() : String {
    return "${this.roundWithTwoDecimals()} mi"
}

public fun Float.formatDistanceMeters() : String {
    return "${this.roundWithTwoDecimals()} m"
}
