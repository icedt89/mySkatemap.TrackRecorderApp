package com.janhafner.myskatemap.apps.trackrecorder.views.activities.viewfinishedtrack.map

import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.core.types.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.findChildFragmentById
import com.janhafner.myskatemap.apps.trackrecorder.map.ITrackRecorderMap
import com.janhafner.myskatemap.apps.trackrecorder.map.OnTrackRecorderMapReadyCallback
import com.janhafner.myskatemap.apps.trackrecorder.map.TrackRecorderMapFragment
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

internal final class MapTabFragmentPresenter(private val view: MapTabFragment,
                                             private val appSettings: IAppSettings,
                                             private val mustSegmentTrack: Boolean)
    : OnTrackRecorderMapReadyCallback {
    private var trackRecording: TrackRecording? = null

    private val trackRecorderMapFragment: TrackRecorderMapFragment = this.view.findChildFragmentById(R.id.fragment_view_finished_track_map)

    init {
        this.trackRecorderMapFragment.getMapAsync(this)
    }

    public override fun onMapReady(trackRecorderMap: ITrackRecorderMap) {
        trackRecorderMap.trackColor = this.view.context!!.getColor(R.color.accentColor)
        trackRecorderMap.showPositions = this.appSettings.showPositionsOnMap
    }

    private fun setupTrack() {
        Observable.fromArray(this.trackRecording!!.locations)
                .subscribeOn(Schedulers.computation())
                .map {
                    val result = mutableListOf<() -> Unit>()

                    if (this.mustSegmentTrack) {
                        var currentSegmentNumber = 0
                        for (group in it.groupBy { it.segmentNumber }) {
                            if (group.key > currentSegmentNumber) {
                                result.add {
                                    this.trackRecorderMapFragment.beginNewTrackSegment()
                                }

                                currentSegmentNumber = group.key
                            }

                            result.add {
                                this.trackRecorderMapFragment.addLocations(group.value)
                            }
                        }
                    } else {
                        result.add {
                            this.trackRecorderMapFragment.addLocations(it)
                        }
                    }

                    result
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