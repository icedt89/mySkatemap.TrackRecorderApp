package com.janhafner.myskatemap.apps.trackrecorder.map.google

import android.graphics.Color
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline

internal final class TrackSegment(private val polyline: Polyline) {
    private val locations: MutableList<LatLng> = ArrayList()

    public fun addLocations(locations: List<LatLng>) {
        this.locations.addAll(locations)

        this.polyline.points = this.locations
    }

    public var polylineColor: Int = Color.RED
        set(value) {
            this.polyline.color = value

            field = value
        }

    public fun remove() {
        this.locations.clear()

        this.polyline.remove()
    }
}