package com.janhafner.myskatemap.apps.trackrecorder.common.types

import org.joda.time.DateTime

public final class Location {
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
}

