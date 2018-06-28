package com.janhafner.myskatemap.apps.trackrecorder.views.map

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.SimpleLocation
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline

internal final class OpenStreetMapTrackRecorderMapFragment : TrackRecorderMapFragment() {
    private lateinit var polyline: Polyline

    private val locations: MutableList<SimpleLocation> = ArrayList()

    private lateinit var map: MapView

    public override val track: List<SimpleLocation>
        get() = this.locations

    public override var isReady: Boolean = false
        private set

    public override fun getSnapshotAsync(callback: OnMapSnapshotReadyCallback) {
        val cached = this.map.drawingCache

        callback.onSnapshotReady(cached)
    }

    public override fun getMapAsync(callback: OnTrackRecorderMapReadyCallback) {
        if(callback is OnTrackRecorderMapLoadedCallback) {
            callback.onMapLoaded(this)
        }

        callback.onMapReady(this)

        this.isReady = true
    }

    public override fun addLocations(locations: List<SimpleLocation>) {
        this.locations.addAll(locations)

        this.moveTrackIntoView()
    }

    public override fun clearTrack() {
        this.locations.clear()

        this.moveTrackIntoView()
    }

    private fun moveTrackIntoView() {
        this.polyline.points = this.locations.map { GeoPoint(it.latitude, it.longitude) }

        if (!this.polyline.points.any()) {
            return
        }

        val boundingBox = BoundingBox.fromGeoPoints(this.polyline.points)

        this.map.zoomToBoundingBox(boundingBox.increaseByScale(1.1f), false,0)

        Log.v("OpenStreetMap", "Moved view of OpenStreet map to new bounds")
    }

    public override fun zoomToLocation(location: SimpleLocation, zoom: Float) {
        this.map.controller.setCenter(GeoPoint(location.latitude, location.longitude))
        this.map.controller.setZoom(16.0 * zoom)
    }

    public override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_openstreetmap_track_recorder_map, container, false)
    }

    public override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.map = this.view!!.findViewById(R.id.trackrecordermapfragment_openstreetmap_mapview)

        this.polyline = Polyline(this.map)
        this.map.overlayManager.add(this.polyline)

        this.applyDefaults()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun applyDefaults() {
        this.map.setBuiltInZoomControls(false)
        this.map.setMultiTouchControls(false)
        this.map.isVerticalMapRepetitionEnabled = false
        this.map.isHorizontalMapRepetitionEnabled = false
        // Inhibit touch gestures which could possibly move/drag the map view around
        this.map.setOnTouchListener {
            _, _ ->
                true
        }
        this.map.setTileSource(TileSourceFactory.MAPNIK)

        this.polyline.color = Color.parseColor("#FFFF3A3C")
    }
}