package com.janhafner.myskatemap.apps.trackrecorder.views.map

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.getApplicationInjector
import com.janhafner.myskatemap.apps.trackrecorder.location.SimpleLocation
import javax.inject.Inject

internal final class TrackRecorderMapFragment : Fragment(), ITrackRecorderMapWithDelayedInitialization {
    @Inject
    public lateinit var trackRecorderMapFragmentFactory: ITrackRecorderMapFragmentFactory

    private lateinit var map: ITrackRecorderMap

    public override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_track_recorder_map, container, false)
    }

    public override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        this.context!!.getApplicationInjector().inject(this)

        super.onViewCreated(view, savedInstanceState)

        val mapFragment = this.trackRecorderMapFragmentFactory.getFragment()

        this.map = mapFragment as ITrackRecorderMap

        this.childFragmentManager.beginTransaction()
                .replace(R.id.fragment_track_recorder_map_placeholder, mapFragment)
                .commit()
    }

    public override fun getMapAsync(callback: OnTrackRecorderMapReadyCallback) {
        if(this.map is ITrackRecorderMapWithDelayedInitialization) {
            (this.map as ITrackRecorderMapWithDelayedInitialization).getMapAsync(callback)
        } else {
            if(callback is OnTrackRecorderMapLoadedCallback) {
                callback.onMapLoaded(this)
            }

            callback.onMapReady(this)
        }
    }

    public override val track: List<SimpleLocation>
        get() = this.map.track

    public override fun addLocations(locations: List<SimpleLocation>) {
        this.map.addLocations(locations)
    }

    public override fun clearTrack() {
        this.map.clearTrack()
    }

    public override fun zoomToLocation(location: SimpleLocation, zoom: Float) {
        this.map.zoomToLocation(location, zoom)
    }
}