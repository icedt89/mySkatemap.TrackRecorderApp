package com.janhafner.myskatemap.apps.trackrecorder.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.janhafner.myskatemap.apps.trackrecorder.R

internal final class TrackRecorderMapFragment : android.support.v4.app.Fragment() {
    public override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_track_recorder_map, container, false)
    }

    public fun getMapAsync(callback: OnTrackRecorderMapReadyCallback) {
        this.childFragmentManager.findFragmentById(R.id.trackrecordermapfragent_googlemap_mapfragment)
        val mapFragment = this.childFragmentManager.findFragmentById(R.id.trackrecordermapfragent_googlemap_mapfragment) as SupportMapFragment

        mapFragment.getMapAsync(OnMapReadyCallback {
            googleMap ->
                val trackRecorderMap = TrackRecorderMap.fromGoogleMapWithDefaults(googleMap, this.context)

                callback.onMapReady(trackRecorderMap)
        })
    }
}

