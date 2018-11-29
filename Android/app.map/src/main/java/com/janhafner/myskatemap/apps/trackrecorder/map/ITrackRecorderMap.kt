package com.janhafner.myskatemap.apps.trackrecorder.map

import androidx.annotation.DrawableRes
import com.janhafner.myskatemap.apps.trackrecorder.common.types.SimpleLocation

public interface ITrackRecorderMap {
    var trackColor: Int

    val isReady: Boolean

    val canAddMarker: Boolean

    val providesNativeMyLocation: Boolean

    var myLocationActivated: Boolean

    var gesturesEnabled: Boolean

    fun addLocations(locations: List<SimpleLocation>)

    fun clearTrack()

    fun zoomToLocation(location: SimpleLocation, zoom: Float)

    fun addMarker(location: SimpleLocation, title: String, @DrawableRes icon: Int? = null): MapMarkerToken

    fun getMapAsync(callback: OnTrackRecorderMapReadyCallback)
}

