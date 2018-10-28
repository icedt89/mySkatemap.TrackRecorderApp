package com.janhafner.myskatemap.apps.trackrecorder.map.google

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.janhafner.myskatemap.apps.trackrecorder.common.types.SimpleLocation
import com.janhafner.myskatemap.apps.trackrecorder.map.MapMarkerToken
import com.janhafner.myskatemap.apps.trackrecorder.map.OnTrackRecorderMapLoadedCallback
import com.janhafner.myskatemap.apps.trackrecorder.map.OnTrackRecorderMapReadyCallback
import com.janhafner.myskatemap.apps.trackrecorder.map.TrackRecorderMapFragment

public final class GoogleTrackRecorderMapFragment : TrackRecorderMapFragment() {
    private lateinit var polyline: Polyline

    private val locations: MutableList<SimpleLocation> = ArrayList()

    private lateinit var map: GoogleMap

    public override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_google_track_recorder_map, container, false)
    }

    public override fun getMapAsync(callback: OnTrackRecorderMapReadyCallback) {
        val mapFragment = this.childFragmentManager.findFragmentById(R.id.trackrecordermapfragment_googlemap_mapfragment) as SupportMapFragment

        mapFragment.getMapAsync({
            this.map = it

            this.polyline = this.map.addPolyline(PolylineOptions())

            this.applyDefaults()

            this.isReady = true

            if(callback is OnTrackRecorderMapLoadedCallback) {
                it.setOnMapLoadedCallback {
                    callback.onMapLoaded(this)
                }
            }

            callback.onMapReady(this)
        })
    }

    public override val providesNativeMyLocation: Boolean = true

    public override var myLocationActivated: Boolean = false
        set(value) {
            if(!this.providesNativeMyLocation) {
                throw UnsupportedOperationException()
            }

            if (this.isReady) {
                this.setMyLocation(value)
            }

            field = value
        }

    public override var isReady: Boolean = false
        private set

    public override val canAddMarker: Boolean = true

    public override val track: List<SimpleLocation>
        get() = this.locations

    public override var gesturesEnabled: Boolean = true
        set(value) {
            if (this.isReady) {
                this.map.uiSettings.setAllGesturesEnabled(value)
            }

            field = value
        }

    public override var trackColor: Int = Color.RED
        set(value) {
            if (this.isReady) {
                this.polyline.color = value
            }

            field = value
        }

    public override fun addMarker(location: SimpleLocation, title: String, icon: Int?): MapMarkerToken {
        val markerOptions = MarkerOptions()
        markerOptions.title(title)
        markerOptions.position(LatLng(location.latitude, location.longitude))

        if (icon != null) {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(icon))
        }

        val marker = this.map.addMarker(markerOptions)

        return MapMarkerToken({
            marker.remove()
        })
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

    @SuppressLint("MissingPermission")
    private fun applyDefaults()  {
        val uiSettings = this.map.uiSettings
        uiSettings.setAllGesturesEnabled(this.gesturesEnabled)
        uiSettings.isCompassEnabled = true
        uiSettings.isZoomControlsEnabled = false
        uiSettings.isMapToolbarEnabled = false

        this.map.mapType = GoogleMap.MAP_TYPE_NORMAL

        this.polyline.isGeodesic = true
        this.polyline.color = this.trackColor

        this.setMyLocation(this.myLocationActivated)
    }

    private fun locationToLatLng(location: SimpleLocation): LatLng {
        return LatLng(location.latitude, location.longitude)
    }

    @SuppressLint("MissingPermission")
    private fun setMyLocation(value: Boolean) {
        this.map.uiSettings.isMyLocationButtonEnabled = value
        this.map.isMyLocationEnabled = value
    }
}

