package com.janhafner.myskatemap.apps.trackrecorder.map

import android.graphics.Color
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.UiSettings
import com.google.android.gms.maps.model.*

internal final class TrackRecorderMap(private val googleMap: GoogleMap) : ITrackRecorderMap {
    private val polyline: Polyline = this.googleMap.addPolyline(PolylineOptions())

    public override val uiSettings: UiSettings
        get() = this.googleMap.uiSettings

    public override var mapType: Int
        get() = this.googleMap.mapType
        set(value){ this.googleMap.mapType = value }

    public override var trackColor: Int
        get() = this.polyline.color
        set(value){ this.polyline.color = value }

    public override var track : Iterable<LatLng>
        get() = this.polyline.points.asIterable()
        set(value) {
            this.polyline.points = value.toList()

            this.moveTrackIntoView()
        }

    private fun moveTrackIntoView() {
        val points = this.polyline.points
        if(!points.any()) {
            return
        }

        val cameraBoundsBuilder = LatLngBounds.builder()
        points.forEach { position -> cameraBoundsBuilder.include(position) }

        val cameraBounds = cameraBoundsBuilder.build()

        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(cameraBounds, 30)

        this.googleMap.animateCamera(cameraUpdate)
    }

    companion object Factory {
        fun fromGoogleMap(googleMap: GoogleMap): ITrackRecorderMap {
            val trackRecorderMap = TrackRecorderMap(googleMap)

            val uiSettings = googleMap.uiSettings
            uiSettings.setAllGesturesEnabled(false)
            uiSettings.isCompassEnabled = false
            uiSettings.isZoomControlsEnabled = false
            uiSettings.isMapToolbarEnabled = false

            googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL

            trackRecorderMap.trackColor = Color.RED

            return trackRecorderMap
        }

        fun fromGoogleMap(googleMap: GoogleMap, initialLocation: LatLng, initialZoom: Float): ITrackRecorderMap {
            val trackRecorderMap = fromGoogleMap(googleMap)

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, initialZoom))

            return trackRecorderMap
        }
    }
}