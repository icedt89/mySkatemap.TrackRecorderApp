package com.janhafner.myskatemap.apps.trackrecorder.map.openstreetmap

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.janhafner.myskatemap.apps.trackrecorder.map.*
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

public final class OpenStreetMapTrackRecorderMapFragment : TrackRecorderMapFragment() {
    private val trackSegmentCollection = TrackSegmentCollection {
        object : ILatitudeLongitudeBoundsBuilder {
            private val locations = mutableListOf<MapLocation>()

            public override fun include(location: MapLocation) {
                this.locations.add(location)
            }

            public override fun build(): MapLocationBounds {
                val boundingBox = BoundingBox.fromGeoPoints(this.locations.map {
                    GeoPoint(it.latitude, it.longitude)
                })

                return MapLocationBounds(MapLocation(boundingBox.latSouth, boundingBox.lonWest), MapLocation(boundingBox.latNorth, boundingBox.lonEast))
            }
        }
    }

    private lateinit var map: MapView

    public override var isReady: Boolean = false
        private set

    public override var gesturesEnabled: Boolean = true

    public override val canAddMarker: Boolean = true

    public override fun getMapAsync(callback: OnTrackRecorderMapReadyCallback) {
        this.isReady = true

        if(callback is OnTrackRecorderMapLoadedCallback) {
            callback.onMapLoaded(this)
        }

        callback.onMapReady(this)
    }

    public override var trackColor: Int = Color.RED
        set(value) {
            if(this.isReady) {
                this.trackSegmentCollection.trackColor = value
            }

            field = value
        }

    public override var showPositions: Boolean = false
        set(value) {
            throw NotImplementedError()
        }

    public override val providesNativeMyLocation: Boolean = false

    public override var myLocationActivated: Boolean = false
        set(value) {
            if(!this.providesNativeMyLocation) {
                throw UnsupportedOperationException()
            }

            field = value
        }

    public override fun addLocations(locations: List<MapLocation>) {
        if (!this.trackSegmentCollection.hasSegments) {
            this.createAndAppendNewTrackSegment()
        }

        this.trackSegmentCollection.addLocations(locations)
    }

    public override fun beginNewTrackSegment() {
        if (this.trackSegmentCollection.hasSegments) {
            this.createAndAppendNewTrackSegment()
        }
    }

    public override fun clearTrack() {
        this.trackSegmentCollection.clear()

        this.focusTrack()
    }

    public override fun focusTrack() {
        if (!this.trackSegmentCollection.hasLocations) {
            return
        }

        val cameraBounds = this.trackSegmentCollection.getCameraBounds()

        val boundingBox = BoundingBox.fromGeoPoints(listOf(GeoPoint(cameraBounds.southWest.latitude, cameraBounds.southWest.longitude), GeoPoint(cameraBounds.northEast.latitude, cameraBounds.northEast.longitude)))

        this.map.zoomToBoundingBox(boundingBox.increaseByScale(1.1f), false,0)
    }

    public override fun addMarker(location: MapLocation, title: String, icon: Int?): MapMarkerToken {
        val marker = Marker(this.map)
        marker.title = title
        marker.position = GeoPoint(location.latitude, location.longitude)
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        if (icon != null) {
            marker.icon = this.view!!.resources.getDrawable(icon, null)
        }

        this.map.overlays.add(marker)

        return MapMarkerToken {
            marker.remove(this.map)
        }
    }

    public override fun zoomToLocation(location: MapLocation, zoom: Float) {
        this.map.controller.setCenter(GeoPoint(location.latitude, location.longitude))
        this.map.controller.setZoom(16.0 * zoom)
    }

    public override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_openstreetmap_track_recorder_map, container, false)
    }

    public override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.map = this.view!!.findViewById(R.id.trackrecordermapfragment_openstreetmap_mapview)

        this.applyDefaults()
    }

    private fun createAndAppendNewTrackSegment() {
        val polyline = Polyline(this.map)

        polyline.color = this.trackColor
        polyline.isGeodesic = true

        this.trackSegmentCollection.appendSegment(TrackSegment(polyline, this.map.overlayManager))

        this.map.overlayManager.add(polyline)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun applyDefaults() {
        this.map.setBuiltInZoomControls(false)
        this.map.setMultiTouchControls(false)
        this.map.isVerticalMapRepetitionEnabled = true
        this.map.isHorizontalMapRepetitionEnabled = true
        this.map.setOnTouchListener {
            _, _ ->
                !this.gesturesEnabled
        }
        this.map.setTileSource(TileSourceFactory.MAPNIK)
    }
}