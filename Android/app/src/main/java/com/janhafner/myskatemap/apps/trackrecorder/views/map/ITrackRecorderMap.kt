package com.janhafner.myskatemap.apps.trackrecorder.views.map

import com.janhafner.myskatemap.apps.trackrecorder.SimpleLocation

internal interface ITrackRecorderMap {
    val track: List<SimpleLocation>

    fun addLocations(locations: List<SimpleLocation>)

    fun clearTrack()

    fun zoomToLocation(location: SimpleLocation, zoom: Float)

    fun getMapAsync(callback: OnTrackRecorderMapReadyCallback)

    fun getSnapshotAsync(callback: OnMapSnapshotReadyCallback)
}

