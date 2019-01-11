package com.janhafner.myskatemap.apps.trackrecorder.map.openstreetmap

import android.graphics.Color
import com.janhafner.myskatemap.apps.trackrecorder.map.ITrackSegment
import com.janhafner.myskatemap.apps.trackrecorder.map.MapLocation
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.OverlayManager
import org.osmdroid.views.overlay.Polyline

internal final class TrackSegment(private val polyline: Polyline, private val overlayManager: OverlayManager) : ITrackSegment {
    private val locations: MutableList<MapLocation> = ArrayList()

    public override fun addLocations(locations: List<MapLocation>) {
        this.locations.addAll(locations)

        val points = this.locations.map { GeoPoint(it.latitude, it.longitude) }
        this.polyline.setPoints(points)
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

        this.overlayManager.remove(this.polyline)
    }
}