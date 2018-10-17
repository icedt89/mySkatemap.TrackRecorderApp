package com.janhafner.myskatemap.apps.trackrecorder.map

import android.support.annotation.DrawableRes
import com.janhafner.myskatemap.apps.trackrecorder.common.types.SimpleLocation

public interface ITrackRecorderMap {
    val track: List<SimpleLocation>

    var trackColor: Int

    val isReady: Boolean

    val canAddMarker: Boolean

    var gesturesEnabled: Boolean

    fun addLocations(locations: List<SimpleLocation>)

    fun clearTrack()

    fun zoomToLocation(location: SimpleLocation, zoom: Float)

    fun addMarker(location: SimpleLocation, title: String, @DrawableRes icon: Int? = null): MapMarkerToken

    fun getMapAsync(callback: OnTrackRecorderMapReadyCallback)
}

