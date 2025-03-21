package com.janhafner.myskatemap.apps.activityrecorder.map.google

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.janhafner.myskatemap.apps.activityrecorder.core.ToastManager
import com.janhafner.myskatemap.apps.activityrecorder.core.formatTimeOnlyDefault
import com.janhafner.myskatemap.apps.activityrecorder.core.types.Location
import com.janhafner.myskatemap.apps.activityrecorder.map.*

public final class GoogleTrackRecorderMapFragment : TrackRecorderMapFragment() {
    private val trackSegmentCollection = TrackSegmentCollection {
        object : ILatitudeLongitudeBoundsBuilder {
            private val builder = LatLngBounds.builder()

            public override fun include(location: Location) {
                this.builder.include(locationToLatLng(location))
            }

            public override fun build(): LocationBounds {
                val bounds = this.builder.build()

                return LocationBounds(Location.simple(bounds.southwest.latitude, bounds.southwest.longitude), Location.simple(bounds.northeast.latitude, bounds.northeast.longitude))
            }
        }
    }

    private lateinit var map: GoogleMap

    private val positionCircles = mutableListOf<Circle>()

    public override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_google_track_recorder_map, container, false)
    }

    private fun getMapFragment(): SupportMapFragment {
        return this.childFragmentManager.findFragmentById(R.id.trackrecordermapfragment_googlemap_mapfragment) as SupportMapFragment
    }

    public override fun getMapAsync(callback: OnTrackRecorderMapReadyCallback) {
        val mapFragment = this.getMapFragment()

        mapFragment.getMapAsync {
            this.map = it

            this.applyDefaults()

            this.isReady = true

            if (callback is OnTrackRecorderMapLoadedCallback) {
                it.setOnMapLoadedCallback {
                    callback.onMapLoaded(this)
                }
            }

            callback.onMapReady(this)
        }
    }

    public override var showPositions: Boolean = false
        set(value) {
            for (positionCircle in this.positionCircles.filter { it.isVisible != value }) {
                positionCircle.isVisible = value
                positionCircle.isClickable = value
            }

            field = value
        }

    public override val providesNativeMyLocation: Boolean = true

    public override var myLocationActivated: Boolean = false
        set(value) {
            if (!this.providesNativeMyLocation) {
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
                this.trackSegmentCollection.trackColor = value
            }

            field = value
        }

    public override fun addMarker(location: Location, title: String, icon: Int?): MapMarkerToken {
        val markerOptions = MarkerOptions()
        markerOptions.title(title)
        markerOptions.position(locationToLatLng(location))

        if (icon != null) {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(icon))
        }

        val marker = this.map.addMarker(markerOptions)

        return MapMarkerToken {
            marker.remove()
        }
    }

    public override fun addLocations(locations: List<Location>) {
        if (!this.trackSegmentCollection.hasSegments) {
            this.createAndAppendNewTrackSegment()
        }

        this.trackSegmentCollection.addLocations(locations)
        val positionCircles = locations.map {
            this.addPositionCircle(it)
        }
        this.positionCircles.addAll(positionCircles)
    }

    private fun addPositionCircle(location: Location): Circle {
        val circleOptions = CircleOptions()
        circleOptions.center(locationToLatLng(location))

        circleOptions.strokeColor(this.trackColor)
        circleOptions.strokeWidth(2.0f)

        circleOptions.fillColor(Color.argb(25, 0, 145, 234))
        if (location.accuracy == null) {
            circleOptions.radius(1.0)
        } else {
            circleOptions.radius(location.accuracy!!.toDouble())
        }

        circleOptions.visible(this.showPositions)
        circleOptions.clickable(this.showPositions)

        val result = this.map.addCircle(circleOptions)
        result.tag = "${location.provider} [${location.segmentNumber}] ${location.time.formatTimeOnlyDefault()} A:${location.accuracy}m S:${location.speed}m/s Alt:${location.altitude}m"

        return result
    }

    public override fun beginNewTrackSegment() {
        if (this.trackSegmentCollection.hasSegments) {
            this.createAndAppendNewTrackSegment()
        }
    }

    public override fun clearTrack() {
        this.positionCircles.clear()
        this.trackSegmentCollection.clear()
        this.map.clear()

        this.focusTrack()
    }

    public override fun focusTrack() {
        if (!this.trackSegmentCollection.hasLocations) {
            return
        }

        val trackLatLngBounds = this.getTrackLatLngBounds()

        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(trackLatLngBounds, 100)

        this.map.animateCamera(cameraUpdate)
    }

    private fun getTrackLatLngBounds(): LatLngBounds{
        val cameraBounds = this.trackSegmentCollection.getCameraBounds()

        return LatLngBounds(locationToLatLng(cameraBounds.southWest), locationToLatLng(cameraBounds.northEast))
    }

    public override fun zoomToLocation(location: Location, zoom: Float) {
        val latLng = GoogleTrackRecorderMapFragment.locationToLatLng(location)

        this.map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }

    @SuppressLint("MissingPermission")
    private fun applyDefaults() {
        val uiSettings = this.map.uiSettings
        uiSettings.setAllGesturesEnabled(this.gesturesEnabled)
        uiSettings.isCompassEnabled = true
        uiSettings.isZoomControlsEnabled = false
        uiSettings.isMapToolbarEnabled = false

        this.map.mapType = GoogleMap.MAP_TYPE_NORMAL
        this.map.setMapStyle(MapStyleOptions("[\n" +
                "    {\n" +
                "        \"featureType\": \"water\",\n" +
                "        \"elementType\": \"geometry.fill\",\n" +
                "        \"stylers\": [\n" +
                "            {\n" +
                "                \"color\": \"#d3d3d3\"\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"featureType\": \"transit\",\n" +
                "        \"stylers\": [\n" +
                "            {\n" +
                "                \"color\": \"#808080\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"visibility\": \"off\"\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"featureType\": \"road.highway\",\n" +
                "        \"elementType\": \"geometry.stroke\",\n" +
                "        \"stylers\": [\n" +
                "            {\n" +
                "                \"visibility\": \"on\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"color\": \"#b3b3b3\"\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"featureType\": \"road.highway\",\n" +
                "        \"elementType\": \"geometry.fill\",\n" +
                "        \"stylers\": [\n" +
                "            {\n" +
                "                \"color\": \"#ffffff\"\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"featureType\": \"road.local\",\n" +
                "        \"elementType\": \"geometry.fill\",\n" +
                "        \"stylers\": [\n" +
                "            {\n" +
                "                \"visibility\": \"on\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"color\": \"#ffffff\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"weight\": 1.8\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"featureType\": \"road.local\",\n" +
                "        \"elementType\": \"geometry.stroke\",\n" +
                "        \"stylers\": [\n" +
                "            {\n" +
                "                \"color\": \"#d7d7d7\"\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"featureType\": \"poi\",\n" +
                "        \"elementType\": \"geometry.fill\",\n" +
                "        \"stylers\": [\n" +
                "            {\n" +
                "                \"visibility\": \"on\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"color\": \"#ebebeb\"\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"featureType\": \"administrative\",\n" +
                "        \"elementType\": \"geometry\",\n" +
                "        \"stylers\": [\n" +
                "            {\n" +
                "                \"color\": \"#a7a7a7\"\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"featureType\": \"road.arterial\",\n" +
                "        \"elementType\": \"geometry.fill\",\n" +
                "        \"stylers\": [\n" +
                "            {\n" +
                "                \"color\": \"#ffffff\"\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"featureType\": \"road.arterial\",\n" +
                "        \"elementType\": \"geometry.fill\",\n" +
                "        \"stylers\": [\n" +
                "            {\n" +
                "                \"color\": \"#ffffff\"\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"featureType\": \"landscape\",\n" +
                "        \"elementType\": \"geometry.fill\",\n" +
                "        \"stylers\": [\n" +
                "            {\n" +
                "                \"visibility\": \"on\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"color\": \"#efefef\"\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"featureType\": \"road\",\n" +
                "        \"elementType\": \"labels.text.fill\",\n" +
                "        \"stylers\": [\n" +
                "            {\n" +
                "                \"color\": \"#696969\"\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"featureType\": \"administrative\",\n" +
                "        \"elementType\": \"labels.text.fill\",\n" +
                "        \"stylers\": [\n" +
                "            {\n" +
                "                \"visibility\": \"on\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"color\": \"#737373\"\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"featureType\": \"poi\",\n" +
                "        \"elementType\": \"labels.icon\",\n" +
                "        \"stylers\": [\n" +
                "            {\n" +
                "                \"visibility\": \"off\"\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"featureType\": \"poi\",\n" +
                "        \"elementType\": \"labels\",\n" +
                "        \"stylers\": [\n" +
                "            {\n" +
                "                \"visibility\": \"off\"\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"featureType\": \"road.arterial\",\n" +
                "        \"elementType\": \"geometry.stroke\",\n" +
                "        \"stylers\": [\n" +
                "            {\n" +
                "                \"color\": \"#d6d6d6\"\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"featureType\": \"road\",\n" +
                "        \"elementType\": \"labels.icon\",\n" +
                "        \"stylers\": [\n" +
                "            {\n" +
                "                \"visibility\": \"off\"\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"featureType\": \"poi\",\n" +
                "        \"elementType\": \"geometry.fill\",\n" +
                "        \"stylers\": [\n" +
                "            {\n" +
                "                \"color\": \"#dadada\"\n" +
                "            }\n" +
                "        ]\n" +
                "    }\n" +
                "]"))

        this.map.setOnCircleClickListener {
            if (it.tag != null && !it.tag!!.toString().isBlank()) {
                ToastManager.showToast(this.context!!, it.tag.toString(), Toast.LENGTH_LONG)
            }
        }
        /*
        this.map.setOnPolylineClickListener {
            it.points.map {
                val location = Location("fused")
                location.latitude = it.latitude
                location.longitude = it.longitude

                location
            }.
        }*/

        this.setMyLocation(this.myLocationActivated)
    }

    private fun createAndAppendNewTrackSegment() {
        val polyline = this.map.addPolyline(PolylineOptions())
        polyline.color = this.trackColor
        polyline.isGeodesic = true

        this.trackSegmentCollection.appendSegment(TrackSegment(polyline))
    }

    @SuppressLint("MissingPermission")
    private fun setMyLocation(value: Boolean) {
        this.map.uiSettings.isMyLocationButtonEnabled = false
        this.map.isMyLocationEnabled = value
    }

    companion object {
        internal fun locationToLatLng(location: Location): LatLng {
            return LatLng(location.latitude, location.longitude)
        }
    }
}