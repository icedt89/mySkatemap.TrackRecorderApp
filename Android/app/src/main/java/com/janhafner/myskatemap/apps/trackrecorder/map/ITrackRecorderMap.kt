package com.janhafner.myskatemap.apps.trackrecorder.map

import com.google.android.gms.maps.UiSettings
import com.google.android.gms.maps.model.LatLng

internal interface ITrackRecorderMap {
    val uiSettings: UiSettings

    var mapType: Int

    var trackColor: Int

    var track: Iterable<LatLng>

    fun zoomToLocation(location: LatLng, zoom: Float)
}