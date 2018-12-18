package com.janhafner.myskatemap.apps.trackrecorder.map

import androidx.annotation.DrawableRes

public interface ITrackRecorderMap {
    var trackColor: Int

    val isReady: Boolean

    val canAddMarker: Boolean

    val providesNativeMyLocation: Boolean

    var myLocationActivated: Boolean

    var gesturesEnabled: Boolean

    fun addLocations(locations: List<MapLocation>)

    fun beginNewTrackSegment()

    fun clearTrack()

    fun zoomToLocation(location: MapLocation, zoom: Float)

    fun addMarker(location: MapLocation, title: String, @DrawableRes icon: Int? = null): MapMarkerToken

    fun getMapAsync(callback: OnTrackRecorderMapReadyCallback)

    fun focusTrack()
}

