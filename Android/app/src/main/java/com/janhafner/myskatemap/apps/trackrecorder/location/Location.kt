package com.janhafner.myskatemap.apps.trackrecorder.location

import android.location.Location
import org.joda.time.DateTime
import java.util.*

internal final class Location {
    public constructor() {
        this.bearing = null;
        this.altitude = null;
        this.latitude = 0.0;
        this.longitude = 0.0;
        this.speed = null;
        this.provider = "";
        this.capturedAt = DateTime.now();
    }

    public var bearing: Float?;

    public var altitude: Double?;

    public var latitude: Double;

    public var longitude: Double;

    public var speed: Float?;

    public var provider: String;

    public var capturedAt: DateTime;

    // SDK-Level: 26 required
    // public var bearingAccuracyDegrees: Float = 0.0f;
    // public var accuracy: Float = 0.0f;
    // public var speedAccuracyMetersPerSecond: Float = 0.0f;
    // public var verticalAccuracyMeters: Float = 0.0f;
}