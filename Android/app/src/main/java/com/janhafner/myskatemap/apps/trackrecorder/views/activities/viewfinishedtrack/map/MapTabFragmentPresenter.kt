package com.janhafner.myskatemap.apps.trackrecorder.views.activities.viewfinishedtrack.map

import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.common.types.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.map.ITrackRecorderMap
import com.janhafner.myskatemap.apps.trackrecorder.map.OnTrackRecorderMapReadyCallback
import com.janhafner.myskatemap.apps.trackrecorder.map.TrackRecorderMapFragment
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.toMapLocation
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

internal final class MapTabFragmentPresenter(private val view: MapTabFragment,
                                             private val trackRecorderMapFragment: TrackRecorderMapFragment,
                                             private val appSettings: IAppSettings)
    : OnTrackRecorderMapReadyCallback {
    private var trackRecording: TrackRecording? = null

    init {
        this.setupMapFragment(this.trackRecorderMapFragment)
    }

    private fun setupMapFragment(trackRecorderMapFragment: TrackRecorderMapFragment) {
        // Iam aware that using commitAllowingStateLoss() should only be used as a last resort!
        // But I don`t know how to handle the fragment replacement any other.
        this.view.childFragmentManager.beginTransaction()
                .replace(R.id.fragment_track_recorder_map_map_placeholder, trackRecorderMapFragment)
                .runOnCommit {
                    trackRecorderMapFragment.getMapAsync(this)
                }
                .commitAllowingStateLoss() // <---- Evil!
    }

    public override fun onMapReady(trackRecorderMap: ITrackRecorderMap) {
        trackRecorderMap.trackColor = this.view.context!!.getColor(R.color.accentColor)
        trackRecorderMap.showPositions = this.appSettings.showPositionsOnMap

        this.setupTrack()
    }

    private fun setupTrack() {
        if(this.trackRecording == null || !this.trackRecorderMapFragment.isReady) {
            return
        }

        Observable.fromArray(this.trackRecording!!.locations)
                .subscribeOn(Schedulers.computation())
                .map {
                    val polylineFactories = mutableListOf<() -> Unit>()

                    var currentSegmentNumber = 0
                    for (group in it.groupBy { it.segmentNumber }) {
                        if(group.key > currentSegmentNumber) {
                            this.trackRecorderMapFragment.beginNewTrackSegment()

                            currentSegmentNumber = group.key
                        }

                        polylineFactories.add({
                            this.trackRecorderMapFragment.addLocations(group.value.map {
                                it.toMapLocation()
                            })
                        })
                    }

                    polylineFactories
                }

                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    for (function in it) {
                        function()
                    }

                    this.trackRecorderMapFragment.focusTrack()
                }
    }

    public fun setTrackRecording(trackRecording: TrackRecording) {
        this.trackRecording = trackRecording

        this.setupTrack()
    }
}