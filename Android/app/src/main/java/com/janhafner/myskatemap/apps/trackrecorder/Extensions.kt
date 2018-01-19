package com.janhafner.myskatemap.apps.trackrecorder

import android.os.Build
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.janhafner.myskatemap.apps.trackrecorder.location.Location
import com.janhafner.myskatemap.apps.trackrecorder.map.ITrackRecorderMap
import io.reactivex.functions.Consumer
import org.joda.time.DateTime

internal fun Location.toLatLng() : LatLng {
    return LatLng(this.latitude, this.longitude)
}

@Deprecated("If receiving locations in chunks is implemented for the map, removed this method and use consumeMany(...) instead!")
internal fun ITrackRecorderMap.consume() : Consumer<in Location> {
    return Consumer({
        location: Location ->
            Log.v("ITrackRecorderMap", "Received location: ${location}")

            val points = this.track.toMutableList()
            points.add(location.toLatLng())

            this.track = points
    })
}

internal fun Location.clone(sequenceNumber : Int) : Location {
    val result = Location(sequenceNumber)

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

internal fun ITrackRecorderMap.consumeMany() : Consumer<Iterable<Location>> {
    return Consumer({
        locations: Iterable<Location> ->
            Log.v("ITrackRecorderMap", "Received locations: ${locations.count()}")

            val points = this.track.toMutableList()
            points.addAll(locations
                    .sortedBy { location -> location.sequenceNumber }
                    .map { location -> location.toLatLng() }
            )

            this.track = points
    })
}

private fun Location.toLiteAndroidLocation() : android.location.Location {
    val result = android.location.Location(this.provider)

    if(this.bearing != null) {
        result.bearing = this.bearing!!
    }

    result.latitude = this.latitude
    result.longitude = this.longitude

    return result
}

internal fun Location.distanceTo(location : Location) : Float {
    val androidLocation = this.toLiteAndroidLocation()
    val otherAndroidLocation = location.toLiteAndroidLocation()

    return androidLocation.distanceTo(otherAndroidLocation)
}

internal fun android.location.Location.toLocation(sequenceNumber : Int) : Location {
    val result = Location(sequenceNumber)

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