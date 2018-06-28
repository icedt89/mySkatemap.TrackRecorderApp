package com.janhafner.myskatemap.apps.trackrecorder.services.temperature

import com.couchbase.lite.Dictionary
import com.couchbase.lite.MutableDictionary
import com.janhafner.myskatemap.apps.trackrecorder.io.data.Location
import org.joda.time.DateTime

internal final class Temperature(public val celsius: Float) {
    public val fahrenheit: Float

    public val kelvin: Float

    public var capturedAt: DateTime = DateTime.now()

    init {
        this.fahrenheit = (this.celsius * Temperature.CELSIUS_TO_FAHRENHEIT_CONVERSION_FACTOR_ONE) + Temperature.CELSIUS_TO_FAHRENHEIT_CONVERSION_FACTOR_TWO
        this.kelvin = this.celsius + Temperature.CELSIUS_TO_KELVIN_CONVERSION_FACTOR
    }

    public override fun toString(): String {
        return "Temperature[c:${this.celsius};f:${this.fahrenheit};k:${this.kelvin}]"
    }

    public fun toCouchDbDictionary() : Dictionary {
        val result = MutableDictionary()

        result.setFloat("celsius", this.celsius)
        result.setDate("capturedAt", this.capturedAt.toDate())

        return result
    }

    companion object {
        private const val CELSIUS_TO_FAHRENHEIT_CONVERSION_FACTOR_ONE: Float = 1.8f
        private const val CELSIUS_TO_FAHRENHEIT_CONVERSION_FACTOR_TWO: Float = 32f

        private const val CELSIUS_TO_KELVIN_CONVERSION_FACTOR: Float = 273.15f

        public val empty: Temperature = Temperature(0.0f)

        public fun fromCouchDbDictionary(dictionary: Dictionary) : Temperature {
            val celsius = dictionary.getFloat("celsius")
            val result = Temperature(celsius)

            result.capturedAt = DateTime(dictionary.getDate("capturedAt"))

            return result
        }
    }
}