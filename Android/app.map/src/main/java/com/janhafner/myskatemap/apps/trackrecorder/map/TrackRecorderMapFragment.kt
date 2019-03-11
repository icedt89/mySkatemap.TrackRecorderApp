package com.janhafner.myskatemap.apps.trackrecorder.map

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment
import com.janhafner.myskatemap.apps.trackrecorder.core.Optional
import com.janhafner.myskatemap.apps.trackrecorder.core.types.Location
import io.reactivex.Single

public abstract class TrackRecorderMapFragment : Fragment(), ITrackRecorderMap {
    public abstract override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View

    public abstract override fun getMapAsync(callback: OnTrackRecorderMapReadyCallback)

    public abstract override val isReady: Boolean

    public abstract override val providesNativeMyLocation: Boolean

    public abstract override var myLocationActivated: Boolean

    public abstract override var trackColor: Int

    public abstract override var gesturesEnabled: Boolean

    public abstract override var showPositions: Boolean

    public abstract override val canAddMarker: Boolean

    public abstract override fun addMarker(location: Location, title: String, @DrawableRes icon: Int?): MapMarkerToken

    public abstract override fun addLocations(locations: List<Location>)

    public abstract override fun beginNewTrackSegment()

    public abstract override fun clearTrack()

    public abstract override fun zoomToLocation(location: Location, zoom: Float)

    public abstract override fun focusTrack()
}