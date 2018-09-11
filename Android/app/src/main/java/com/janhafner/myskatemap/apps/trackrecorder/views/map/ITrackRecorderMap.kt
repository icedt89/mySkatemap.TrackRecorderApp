package com.janhafner.myskatemap.apps.trackrecorder.views.map

import com.janhafner.myskatemap.apps.trackrecorder.common.SimpleLocation

internal interface ITrackRecorderMap {
    val track: List<SimpleLocation>

    val isReady: Boolean

    var gesturesEnabled: Boolean

    fun addLocations(locations: List<SimpleLocation>)

    fun clearTrack()

    fun zoomToLocation(location: SimpleLocation, zoom: Float)

    fun getMapAsync(callback: OnTrackRecorderMapReadyCallback)

    fun getSnapshotAsync(callback: OnMapSnapshotReadyCallback)
}

