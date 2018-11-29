package com.janhafner.myskatemap.apps.trackrecorder.common

import android.app.NotificationManager
import android.content.ClipboardManager
import android.content.Context
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import com.janhafner.myskatemap.apps.trackrecorder.common.types.Location
import com.janhafner.myskatemap.apps.trackrecorder.common.types.SimpleLocation
import io.reactivex.Observable
import org.joda.time.DateTime

public fun Context.startLocationServicesSettingsActivity() {
    this.startActivity(android.content.Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
}

public fun Context.isLocationServicesEnabled(): Boolean {
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        return this.getLocationManager().isLocationEnabled
    }

    return Settings.Secure.getInt(this.contentResolver, Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF) != Settings.Secure.LOCATION_MODE_OFF
}

public fun Context.getLocationManager(): LocationManager {
    return this.getSystemService(LocationManager::class.java)
}

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

private fun Location.toLiteAndroidLocation(): android.location.Location {
    val result = android.location.Location(this.provider)

    if (this.bearing != null) {
        result.bearing = this.bearing!!
    }

    result.latitude = this.latitude
    result.longitude = this.longitude

    return result
}

public fun Location.toSimpleLocation(): SimpleLocation {
    return SimpleLocation(this.latitude, this.longitude)
}

public fun Location.distanceTo(location: Location): Float {
    val androidLocation = this.toLiteAndroidLocation()
    val otherAndroidLocation = location.toLiteAndroidLocation()

    return androidLocation.distanceTo(otherAndroidLocation)
}

public fun Location.isInDistance(location: Location, maximumDistance: Double, includeEdge: Boolean = true): Boolean {
    val distance = this.distanceTo(location)

    if (includeEdge) {
        return distance <= maximumDistance
    }

    return distance < maximumDistance
}

public fun android.location.Location.toLocation(): Location {
    val result = Location()

    result.altitude = this.altitude
    result.latitude = this.latitude
    result.longitude = this.longitude

    result.speed = this.speed
    result.provider = this.provider
    result.capturedAt = DateTime(this.time)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        result.bearingAccuracyDegrees = this.bearingAccuracyDegrees
        result.accuracy = this.accuracy
        result.speedAccuracyMetersPerSecond = this.speedAccuracyMetersPerSecond
        result.verticalAccuracyMeters = this.verticalAccuracyMeters
    }

    return result
}