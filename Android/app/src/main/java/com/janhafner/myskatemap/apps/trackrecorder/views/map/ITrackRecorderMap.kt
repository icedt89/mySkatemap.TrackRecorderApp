package com.janhafner.myskatemap.apps.trackrecorder.views.map

import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.SimpleLocation

internal interface ITrackRecorderMap {
    val track: List<SimpleLocation>

    fun addLocations(locations: List<SimpleLocation>)

    fun clearTrack()

    fun zoomToLocation(location: SimpleLocation, zoom: Float)

    fun getMapAsync(callback: OnTrackRecorderMapReadyCallback)
}

