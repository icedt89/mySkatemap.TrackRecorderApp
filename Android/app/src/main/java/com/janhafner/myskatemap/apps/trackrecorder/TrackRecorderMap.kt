package com.janhafner.myskatemap.apps.trackrecorder

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.UiSettings
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.janhafner.myskatemap.apps.trackrecorder.location.Location

internal final class TrackRecorderMap : ITrackRecorderMap {
    private final val googleMap: GoogleMap;

    private final val polyline: Polyline;

    public constructor(googleMap: GoogleMap) {
        if(googleMap == null) {
            throw IllegalArgumentException("googleMap");
        }

        this.googleMap = googleMap;
        this.polyline = this.googleMap.addPolyline(PolylineOptions());
    }

    public final override val uiSettings: UiSettings
        get() { return this.googleMap.uiSettings;};

    public final override var mapType: Int
        get() { return this.googleMap.mapType;}
        set(value){ this.googleMap.mapType = value; };

    public final override var trackColor: Int
        get() { return this.polyline.color;}
        set(value){ this.polyline.color = value; };

    public final override val trackedPath: Iterable<LatLng>
        get() { return this.polyline.points;}

    public final override fun setRecordedTrack(recordedTrack: Iterable<Location>) {
        this.polyline.points = recordedTrack.map { location -> LatLng(location.latitude, location.longitude); };

        this.moveTrackedPathIntoView();
    }

    private final fun moveTrackedPathIntoView() {
        val points = this.polyline.points;
        if(!points.any()) {
            return;
        }

        val cameraBoundsBuilder = LatLngBounds.builder();
        this.polyline.points.forEach { position -> cameraBoundsBuilder.include(position); };


        val cameraBounds = cameraBoundsBuilder.build();

        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(cameraBounds, 30);

        this.googleMap.animateCamera(cameraUpdate);
    }

    companion object Factory {
        fun fromGoogleMap(googleMap: GoogleMap): ITrackRecorderMap {
            if(googleMap == null) {
                throw IllegalArgumentException("googleMap");
            }

            val trackRecorderMap = TrackRecorderMap(googleMap);

            val uiSettings = googleMap.uiSettings;
            uiSettings.setAllGesturesEnabled(false);
            uiSettings.isCompassEnabled = false;
            uiSettings.isZoomControlsEnabled = false;
            uiSettings.isMapToolbarEnabled = false;

            googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL;

            trackRecorderMap.trackColor = R.color.colorTrackedPathFill;

            return trackRecorderMap;
        }

        fun fromGoogleMap(googleMap: GoogleMap, initialLocation: LatLng, initialZoom: Float): ITrackRecorderMap {
            if(googleMap == null) {
                throw IllegalArgumentException("googleMap");
            }

            if(initialLocation == null) {
                throw IllegalArgumentException("initialLocation");
            }

            if(initialZoom == null) {
                throw IllegalArgumentException("initialZoom");
            }

            val trackRecorderMap = fromGoogleMap(googleMap);

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, initialZoom));

            return trackRecorderMap;
        }
    }
}