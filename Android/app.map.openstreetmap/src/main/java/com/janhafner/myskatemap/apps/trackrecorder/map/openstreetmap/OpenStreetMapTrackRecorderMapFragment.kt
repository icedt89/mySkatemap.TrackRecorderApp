package com.janhafner.myskatemap.apps.trackrecorder.map.openstreetmap

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.janhafner.myskatemap.apps.trackrecorder.common.types.SimpleLocation
import com.janhafner.myskatemap.apps.trackrecorder.map.MapMarkerToken
import com.janhafner.myskatemap.apps.trackrecorder.map.OnTrackRecorderMapLoadedCallback
import com.janhafner.myskatemap.apps.trackrecorder.map.OnTrackRecorderMapReadyCallback
import com.janhafner.myskatemap.apps.trackrecorder.map.TrackRecorderMapFragment
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

public final class OpenStreetMapTrackRecorderMapFragment : TrackRecorderMapFragment() {
    private lateinit var polyline: Polyline

    private val locations: MutableList<SimpleLocation> = ArrayList()

    private lateinit var map: MapView

    public override val track: List<SimpleLocation>
        get() = this.locations

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


    public override var trackColor: Int
        get() {
            if(!this.isReady) {
                throw IllegalStateException("Map needs to be initialized first!")
            }

            return this.polyline.color
        }
        set(value) {
            if(!this.isReady) {
                throw IllegalStateException("Map needs to be initialized first!")
            }

            this.polyline.color = value
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
        val points = this.locations.map { GeoPoint(it.latitude, it.longitude) }
        this.polyline.setPoints(points)

        if (!this.polyline.points.any()) {
            return
        }

        val boundingBox = BoundingBox.fromGeoPoints(this.polyline.points)

        this.map.zoomToBoundingBox(boundingBox.increaseByScale(1.1f), false,0)
    }

    public override fun addMarker(location: SimpleLocation, title: String, icon: Int?): MapMarkerToken {
        throw NotImplementedError("This function is not stable at the moment.")

        val marker = Marker(this.map)
        marker.title = title
        marker.position = GeoPoint(location.latitude, location.longitude)
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        if (icon != null) {
            marker.icon = this.view!!.resources.getDrawable(icon, null)
        }

        this.map.overlays.add(marker)

        return MapMarkerToken({
            marker.remove(this.map)
        })
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
        this.map.isVerticalMapRepetitionEnabled = true
        this.map.isHorizontalMapRepetitionEnabled = true
        this.map.setOnTouchListener {
            _, _ ->
                !this.gesturesEnabled
        }
        this.map.setTileSource(TileSourceFactory.MAPNIK)

        this.polyline.isGeodesic = true
        this.polyline.color = Color.RED
    }
}