package com.janhafner.myskatemap.apps.trackrecorder.map

import android.graphics.Color

public final class TrackSegmentCollection(private val latitudeLongitudeBoundsBuilderFactory: () -> ILatitudeLongitudeBoundsBuilder) {
    private val segments: MutableCollection<ITrackSegment> = mutableListOf()

    private var currentTrackSegment: ITrackSegment? = null

    private var cameraBoundsBuilder: ILatitudeLongitudeBoundsBuilder = this.latitudeLongitudeBoundsBuilderFactory()

    public val hasSegments: Boolean
        get() = this.segments.any()

    public val hasLocations: Boolean
        get() = this.segments.any { it.hasLocations }

    public fun appendSegment(trackSegment: ITrackSegment) {
        this.segments.add(trackSegment)

        this.currentTrackSegment = trackSegment
    }

    public fun addLocations(locations: List<MapLocation>) {
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

    public fun getCameraBounds(): MapLocationBounds {
        if(!this.hasSegments) {
            throw IllegalStateException("No segments!")
        }

        return this.cameraBoundsBuilder.build()
    }

    public fun clear() {
        this.segments.forEach { it.remove() }
        this.segments.clear()

        this.currentTrackSegment = null

        this.cameraBoundsBuilder = this.latitudeLongitudeBoundsBuilderFactory()
    }
}