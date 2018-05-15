package com.janhafner.myskatemap.apps.trackrecorder.infrastructure

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

    public override fun toString(): String {
        return "Location(#${this.sequenceNumber}; lat: ${this.latitude}; lon: ${this.longitude})"
    }
}