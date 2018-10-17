package com.janhafner.myskatemap.apps.trackrecorder.views.map

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.janhafner.myskatemap.apps.trackrecorder.common.types.SimpleLocation

internal abstract class TrackRecorderMapFragment : Fragment(), ITrackRecorderMap {
    public abstract override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View

    public abstract override fun getMapAsync(callback: OnTrackRecorderMapReadyCallback)

    public abstract override val track: List<SimpleLocation>

    public abstract override val isReady: Boolean

    public abstract override var gesturesEnabled: Boolean

    public abstract override val canAddMarker: Boolean

    public abstract override fun addLocations(locations: List<SimpleLocation>)

    public abstract override fun clearTrack()

    public abstract override fun zoomToLocation(location: SimpleLocation, zoom: Float)
}