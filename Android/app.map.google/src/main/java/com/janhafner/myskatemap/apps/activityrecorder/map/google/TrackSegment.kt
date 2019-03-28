package com.janhafner.myskatemap.apps.activityrecorder.map.google

import android.graphics.Color
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.janhafner.myskatemap.apps.activityrecorder.core.types.Location
import com.janhafner.myskatemap.apps.activityrecorder.map.ITrackSegment

internal final class TrackSegment(private val polyline: Polyline) : ITrackSegment {
    private val locations: MutableList<Location> = ArrayList()

    public override fun addLocations(locations: List<Location>) {
        this.locations.addAll(locations)

        this.polyline.points = this.locations.map { LatLng(it.latitude, it.longitude) }
    }

    public override var show: Boolean
        get() = this.polyline.isVisible
        set(value) {
            this.polyline.isVisible = value
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