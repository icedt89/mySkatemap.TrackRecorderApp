package com.janhafner.myskatemap.apps.trackrecorder.common.types

import org.joda.time.DateTime

public final class Location(
        public val provider: String,
        public val time: DateTime,
        public var latitude: Double,
        public var longitude: Double) {
    public var altitude: Double? = null

    public var speed: Float? = null

    public var bearing: Float? = null

    public var accuracy: Float? = null

    public var verticalAccuracyMeters: Float? = null

    public var speedAccuracyMetersPerSecond: Float? = null

    public var bearingAccuracyDegrees: Float? = null

    public var segmentNumber: Int = 0
}
