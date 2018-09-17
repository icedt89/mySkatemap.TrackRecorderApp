package com.janhafner.myskatemap.apps.trackrecorder.services

import android.os.Build
import com.janhafner.myskatemap.apps.trackrecorder.common.SimpleLocation
import com.janhafner.myskatemap.apps.trackrecorder.common.pairWithPrevious
import com.janhafner.myskatemap.apps.trackrecorder.services.models.Location
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.SingleEmitter
import org.joda.time.DateTime
import org.joda.time.Seconds
import java.util.*

private fun Location.toLiteAndroidLocation(): android.location.Location {
    val result = android.location.Location(this.provider)

    if (this.bearing != null) {
        result.bearing = this.bearing!!
    }

    result.latitude = this.latitude
    result.longitude = this.longitude

    return result
}

public fun Location.distanceTo(location: Location): Float {
    val androidLocation = this.toLiteAndroidLocation()
    val otherAndroidLocation = location.toLiteAndroidLocation()

    return androidLocation.distanceTo(otherAndroidLocation)
}

public fun Location.toSimpleLocation(): SimpleLocation {
    return SimpleLocation(this.latitude, this.longitude)
}

public fun Location.isInDistance(location: Location, maximumDistance: Double, includeEdge: Boolean = true): Boolean {
    val distance = this.distanceTo(location)

    if (includeEdge) {
        return distance <= maximumDistance
    }

    return distance < maximumDistance
}

public fun Location.clone(): Location {
    val result = Location()

    result.latitude = this.latitude
    result.longitude = this.longitude
    result.provider = this.provider
    result.bearing = this.bearing
    result.speed = this.speed
    result.accuracy = this.accuracy
    result.bearingAccuracyDegrees = this.bearingAccuracyDegrees
    result.speedAccuracyMetersPerSecond = this.speedAccuracyMetersPerSecond
    result.verticalAccuracyMeters = this.verticalAccuracyMeters
    result.altitude = this.altitude

    return result
}


public fun Observable<Location>.calculateMissingSpeed(): Observable<Location> {
    return this
            .pairWithPrevious()
            .map {
                if(it.first != null && it.second != null && it.second!!.speed == null) {
                    val distanceInMeters = it.first!!.distanceTo(it.second!!)
                    if (distanceInMeters != 0.0f) {
                        val locationSecondsDifference = Seconds.secondsBetween(it.second!!.capturedAt, it.first!!.capturedAt)

                        it.second!!.speed = Math.abs(locationSecondsDifference.seconds) / distanceInMeters
                    } else {
                        it.second!!.speed = 0.0f
                    }
                }

                it.second
            }
}

public fun Observable<Location>.dropLocationsNotInDistance(maximumDistance: Double, includeEdge: Boolean = true): Observable<Location> {
    return this.pairWithPrevious()
            .filter {
                if (it.first == null) {
                    // Dont filter first real value [Pair(null, location)]
                    true
                } else {
                    // Apply distance filter to suppress emitting values to close
                    !it.second!!.isInDistance(it.first!!, maximumDistance, includeEdge)
                }
            }
            .map {
                it.second
            }
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