package com.janhafner.myskatemap.apps.trackrecorder.views.map

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.SimpleLocation

internal final class GoogleTrackRecorderMapFragment : TrackRecorderMapFragment() {
    private lateinit var polyline: Polyline

    private val locations: MutableList<SimpleLocation> = ArrayList()

    private lateinit var map: GoogleMap

    public override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_google_track_recorder_map, container, false)
    }

    public override fun getSnapshotAsync(callback: OnMapSnapshotReadyCallback) {
        this.map.snapshot {
            callback.onSnapshotReady(it)
        }
    }

    public override fun getMapAsync(callback: OnTrackRecorderMapReadyCallback) {
        val mapFragment = this.childFragmentManager.findFragmentById(R.id.trackrecordermapfragment_googlemap_mapfragment) as SupportMapFragment

        mapFragment.getMapAsync({
            this.map = it

            this.polyline = this.map.addPolyline(PolylineOptions())

            this.applyDefaults()

            if(callback is OnTrackRecorderMapLoadedCallback) {
                it.setOnMapLoadedCallback {
                    callback.onMapLoaded(this)
                }
            }

            callback.onMapReady(this)

            this.isReady = true
        })
    }

    public override var isReady: Boolean = false
        private set

    public override val track: List<SimpleLocation>
        get() = this.locations

    public override var gesturesEnabled: Boolean = false
        set(value) {
            if (this.isReady) {
                this.map.uiSettings.setAllGesturesEnabled(value)
            }

            field = value
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
        val locationsAsLatLng = this.locations.map { locationToLatLng(it) }
        this.polyline.points = locationsAsLatLng

        if (!locationsAsLatLng.any()) {
            return
        }

        val cameraBoundsBuilder = LatLngBounds.builder()
        locationsAsLatLng.forEach { cameraBoundsBuilder.include(it) }

        val cameraBounds = cameraBoundsBuilder.build()

        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(cameraBounds, 100)

        this.map.animateCamera(cameraUpdate)
    }

    public override fun zoomToLocation(location: SimpleLocation, zoom: Float) {
        val latLng = this.locationToLatLng(location)

        this.map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }

    private fun applyDefaults()  {
        val uiSettings = this.map.uiSettings
        uiSettings.setAllGesturesEnabled(false)
        uiSettings.isCompassEnabled = false
        uiSettings.isZoomControlsEnabled = false
        uiSettings.isMapToolbarEnabled = false

        this.map.mapType = GoogleMap.MAP_TYPE_NORMAL

        this.map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this.context!!, R.raw.mapstyle_fanticmotor))

        this.polyline.color = this.context!!.getColor(R.color.secondaryColor)
    }

    private fun locationToLatLng(location: SimpleLocation): LatLng {
        return LatLng(location.latitude, location.longitude)
    }
}

