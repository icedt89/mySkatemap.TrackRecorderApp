package com.janhafner.myskatemap.apps.trackrecorder.io.data

import com.couchbase.lite.Dictionary
import com.couchbase.lite.MutableDictionary
import org.joda.time.DateTime

internal final class Location(public val sequenceNumber: Int) {
    public var bearing: Float? = null

    public var altitude: Double? = null

    public var latitude: Double = 0.0

    public var longitude: Double = 0.0

    public var speed: Float? = null

    public var provider: String = ""

    public var capturedAt: DateTime = DateTime.now()

    // SDK-Level: 26 required
    public var bearingAccuracyDegrees: Float? = null

    public var accuracy: Float? = null

    public var speedAccuracyMetersPerSecond: Float? = null

    public var verticalAccuracyMeters: Float? = null

    public fun toCouchDbDictionary() : Dictionary {
        val result = MutableDictionary()

        result.setInt("sequenceNumber", this.sequenceNumber)
        result.setString("provider", this.provider)
        result.setDouble("latitude", this.latitude)
        result.setDouble("longitude", this.longitude)
        result.setDate("capturedAt", this.capturedAt.toDate())

        if(this.accuracy != null) {
            result.setFloat("accuracy", this.accuracy!!)
        }

        if(this.altitude != null) {
            result.setDouble("altitude", this.altitude!!)
        }

        if(this.bearing != null) {
            result.setFloat("bearing", this.bearing!!)
        }

        if(this.bearingAccuracyDegrees != null) {
            result.setFloat("bearingAccuracyDegrees", this.bearingAccuracyDegrees!!)
        }

        if(this.speed != null) {
            result.setFloat("speed", this.speed!!)
        }

        if(this.speedAccuracyMetersPerSecond != null) {
            result.setFloat("speedAccuracyMetersPerSecond", this.speedAccuracyMetersPerSecond!!)
        }

        if(this.verticalAccuracyMeters != null) {
            result.setFloat("verticalAccuracyMeters", this.verticalAccuracyMeters!!)
        }

        return result
    }

    public override fun toString(): String {
        return "Location(#${this.sequenceNumber}; lat: ${this.latitude}; lon: ${this.longitude})"
    }

    companion object {
        public fun fromCouchDbDictionary(dictionary: Dictionary) : Location {
            val sequenceNumber = dictionary.getInt("sequenceNumber")
            val result = Location(sequenceNumber)

            result.provider = dictionary.getString("provider")
            result.latitude = dictionary.getDouble("latitude")
            result.longitude = dictionary.getDouble("longitude")
            result.capturedAt = DateTime(dictionary.getDate("capturedAt"))
            result.accuracy = dictionary.getFloat("accuracy")
            result.altitude = dictionary.getDouble("altitude")
            result.bearing = dictionary.getFloat("bearing")
            result.bearingAccuracyDegrees = dictionary.getFloat("bearingAccuracyDegrees")
            result.speed = dictionary.getFloat("speed")
            result.speedAccuracyMetersPerSecond = dictionary.getFloat("speedAccuracyMetersPerSecond")
            result.verticalAccuracyMeters = dictionary.getFloat("verticalAccuracyMeters")

            return result
        }
    }
}