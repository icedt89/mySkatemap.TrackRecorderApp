package com.janhafner.myskatemap.apps.trackrecorder.map

import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import com.janhafner.myskatemap.apps.trackrecorder.core.Optional
import com.janhafner.myskatemap.apps.trackrecorder.core.types.Location
import io.reactivex.Single

public interface ITrackRecorderMap {
    var trackColor: Int

    val isReady: Boolean

    val canAddMarker: Boolean

    val providesNativeMyLocation: Boolean

    var myLocationActivated: Boolean

    var gesturesEnabled: Boolean

    var showPositions: Boolean

    fun addLocations(locations: List<Location>)

    fun beginNewTrackSegment()

    fun clearTrack()

    fun zoomToLocation(location: Location, zoom: Float)

    fun addMarker(location: Location, title: String, @DrawableRes icon: Int? = null): MapMarkerToken

    fun getMapAsync(callback: OnTrackRecorderMapReadyCallback)

    fun focusTrack()
}

