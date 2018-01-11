package com.janhafner.myskatemap.apps.trackrecorder

import com.janhafner.myskatemap.apps.trackrecorder.location.Location
import org.joda.time.DateTime
import java.util.*

internal fun android.location.Location.toLocation() : Location {
    val result = Location();

    result.altitude = this.altitude;
    result.latitude = this.latitude;
    result.longitude = this.longitude;

    result.speed = this.speed;
    result.provider = this.provider;
    result.capturedAt = DateTime(this.time);

    // SDK-Level: 26 required
    // result.bearingAccuracyDegrees = this.bearingAccuracyDegrees;
    // result.accuracy = this.accuracy;
    // result.speedAccuracyMetersPerSecond = this.speedAccuracyMetersPerSecond;
    // result.verticalAccuracyMeters = this.verticalAccuracyMeters;

    return result;
}