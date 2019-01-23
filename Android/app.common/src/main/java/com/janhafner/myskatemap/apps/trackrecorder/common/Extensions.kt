package com.janhafner.myskatemap.apps.trackrecorder.common

import android.app.NotificationManager
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import com.janhafner.myskatemap.apps.trackrecorder.common.types.Location
import io.reactivex.Observable
import org.joda.time.DateTime

public fun Context.getClipboardManager(): ClipboardManager {
    return this.getSystemService(ClipboardManager::class.java)
}

public fun Context.getNotificationManager(): NotificationManager {
    return this.getSystemService(NotificationManager::class.java)
}

public fun Observable<PropertyChangedData>.hasChanged(): Observable<PropertyChangedData> {
    return this.filter {
        it.hasChanged
    }
}

public fun Observable<PropertyChangedData>.isNamed(name: String): Observable<PropertyChangedData> {
    return this.filter {
        it.propertyName == name
    }
}

fun Location.toLiteAndroidLocation(): android.location.Location {
    val result = android.location.Location(this.provider)

    result.latitude = this.latitude
    result.longitude = this.longitude

    return result
}

public fun List<android.location.Location>.calculateDistance(): Float {
    var result = 0.0f

    if(this.count() < 2) {
        return result
    }

    var lastAccessedLocation: android.location.Location = this.first()
    for(location in this.drop(1)) {
        result += lastAccessedLocation.distanceTo(location)

        lastAccessedLocation = location
    }

    return result
}

public fun android.location.Location.isInDistance(location: android.location.Location, maximumDistanceInMeter: Float, includeEdge: Boolean = true): Boolean {
    val distance = this.distanceTo(location)

    if(includeEdge) {
        return distance <= maximumDistanceInMeter
    }

    return distance < maximumDistanceInMeter
}


public fun android.location.Location.toLocation(): Location {
    val result = Location(this.provider, DateTime(this.time), this.latitude, this.longitude)

    if(this.hasAccuracy()) {
        result.accuracy = this.accuracy
    }

    if(this.hasBearing()) {
        result.bearing = this.bearing
    }

    if(this.hasSpeed()) {
        result.speed = this.speed
    }

    if(this.hasAltitude()) {
        result.altitude = this.altitude
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        if(this.hasBearingAccuracy()) {
            result.bearingAccuracyDegrees = this.bearingAccuracyDegrees
        }

        if(this.hasSpeedAccuracy()) {
            result.speedAccuracyMetersPerSecond = this.speedAccuracyMetersPerSecond
        }

        if(this.hasVerticalAccuracy()) {
            result.verticalAccuracyMeters = this.verticalAccuracyMeters
        }
    }

    return result
}