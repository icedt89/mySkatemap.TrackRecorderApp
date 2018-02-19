package com.janhafner.myskatemap.apps.trackrecorder.views.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.UiSettings
import com.google.android.gms.maps.model.*
import com.janhafner.myskatemap.apps.trackrecorder.R

internal final class TrackRecorderMapFragment : android.support.v4.app.Fragment(), ITrackRecorderMap {
    private lateinit var polyline: Polyline

    private lateinit var googleMap: GoogleMap

    public override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_track_recorder_map, container, false)
    }

    public fun getMapAsync(callback: OnTrackRecorderMapReadyCallback) {
        val mapFragment = this.childFragmentManager.findFragmentById(R.id.trackrecordermapfragment_googlemap_mapfragment) as SupportMapFragment

        mapFragment.getMapAsync({
            this.googleMap = it

            this.polyline = this.googleMap.addPolyline(PolylineOptions())

            this.applyDefaults()

            if(callback is OnTrackRecorderMapLoadedCallback) {
                it.setOnMapLoadedCallback {
                    callback.onMapLoaded(this)
                }
            }

            callback.onMapReady(this)
        })
    }

    public override val uiSettings: UiSettings
        get() = this.googleMap.uiSettings

    public override var mapType: Int
        get() = this.googleMap.mapType
        set(value){ this.googleMap.mapType = value }

    public override var trackColor: Int
        get() = this.polyline.color
        set(value){ this.polyline.color = value }

    public override var track: Iterable<LatLng>
        get() = this.polyline.points.asIterable()
        set(value) {
            this.polyline.points = value.toList()

            this.moveTrackIntoView()
        }

    private fun moveTrackIntoView() {
        val points = this.polyline.points
        if (!points.any()) {
            return
        }

        val cameraBoundsBuilder = LatLngBounds.builder()
        points.forEach { cameraBoundsBuilder.include(it) }

        val cameraBounds = cameraBoundsBuilder.build()

        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(cameraBounds, 50)

        this.googleMap.animateCamera(cameraUpdate)
    }

    public override fun zoomToLocation(location: LatLng, zoom: Float) {
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, zoom))
    }

    private fun applyDefaults()  {
        val uiSettings = this.googleMap.uiSettings
        uiSettings.setAllGesturesEnabled(false)
        uiSettings.isCompassEnabled = false
        uiSettings.isZoomControlsEnabled = false
        uiSettings.isMapToolbarEnabled = false

        this.googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        this.googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this.context!!, R.raw.mapstyle_fanticmotor))

        this.trackColor = this.context!!.getColor(R.color.colorTrack)
    }
}

