package com.janhafner.myskatemap.apps.trackrecorder.map.google

import android.graphics.Color
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

internal final class TrackSegmentCollection {
    private val segments: MutableCollection<TrackSegment> = mutableListOf()

    private var currentTrackSegment: TrackSegment? = null

    private var cameraBoundsBuilder = LatLngBounds.builder()

    public val hasSegments: Boolean
        get() = this.segments.any()

    public fun appendSegment(trackSegment: TrackSegment) {
        this.segments.add(trackSegment)

        this.currentTrackSegment = trackSegment
    }

    public fun addLocations(locations: List<LatLng>) {
        if(!this.hasSegments) {
            throw IllegalStateException("No current segment!")
        }

        locations.forEach {
            this.cameraBoundsBuilder.include(it)
        }

        this.currentTrackSegment!!.addLocations(locations)
    }

    public var trackColor: Int = Color.RED
        set(value) {
            this.segments.forEach { it.polylineColor = value }

            field = value
        }

    public fun getCameraBounds(): LatLngBounds {
        if(!this.hasSegments) {
            throw IllegalStateException("No segments!")
        }

        return this.cameraBoundsBuilder.build()
    }

    public fun clear() {
        this.segments.forEach { it.remove() }
        this.segments.clear()

        this.currentTrackSegment = null

        this.cameraBoundsBuilder = LatLngBounds.builder()
    }
}