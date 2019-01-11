package com.janhafner.myskatemap.apps.trackrecorder.map.google

import android.graphics.Color
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.janhafner.myskatemap.apps.trackrecorder.map.ITrackSegment
import com.janhafner.myskatemap.apps.trackrecorder.map.MapLocation

internal final class TrackSegment(private val polyline: Polyline) : ITrackSegment {
    private val locations: MutableList<MapLocation> = ArrayList()

    public override fun addLocations(locations: List<MapLocation>) {
        this.locations.addAll(locations)

        this.polyline.points = this.locations.map { LatLng(it.latitude, it.longitude) }
    }

    public override val hasLocations: Boolean
        get() = this.polyline.points.any()

    public override var polylineColor: Int = Color.RED
        set(value) {
            this.polyline.color = value

            field = value
        }

    public override fun remove() {
        this.locations.clear()

        this.polyline.remove()
    }
}