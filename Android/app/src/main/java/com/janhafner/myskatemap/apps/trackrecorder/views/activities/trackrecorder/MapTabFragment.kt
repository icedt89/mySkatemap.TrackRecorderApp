package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder

import android.graphics.drawable.Icon
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.model.LatLng
import com.jakewharton.rxbinding2.view.clicks
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.consumeLocations
import com.janhafner.myskatemap.apps.trackrecorder.consumeReset
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.ViewHolder
import com.janhafner.myskatemap.apps.trackrecorder.store
import com.janhafner.myskatemap.apps.trackrecorder.views.map.ITrackRecorderMap
import com.janhafner.myskatemap.apps.trackrecorder.views.map.OnTrackRecorderMapLoadedCallback
import com.janhafner.myskatemap.apps.trackrecorder.views.map.OnTrackRecorderMapReadyCallback
import com.janhafner.myskatemap.apps.trackrecorder.views.map.TrackRecorderMapFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

internal final class MapTabFragment: Fragment(), OnTrackRecorderMapReadyCallback, OnTrackRecorderMapLoadedCallback {
    private lateinit var presenter: ITrackRecorderActivityPresenter

    private var currentLocationsChangedSubscription: Disposable? = null

    private val subscriptions: CompositeDisposable = CompositeDisposable()

    private val viewHolder: ViewHolder = ViewHolder()

    public override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_map_tab, container, false)
    }

    public override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.viewHolder
                .store(view.findViewById<FloatingActionButton>(R.id.trackrecorderactivity_tab_map_togglerecording_floatingactionbutton))

        val mapFragment = this.childFragmentManager.findFragmentById(R.id.trackrecorderactivity_tab_map_googlemap) as TrackRecorderMapFragment

        mapFragment.getMapAsync(this)
    }

    public override fun onDestroy() {
        super.onDestroy()

        this.viewHolder.clear()
    }

    public override fun onStart() {
        super.onStart()

        val toggleRecordingFloatingActionButton = this.viewHolder.retrieve<FloatingActionButton>(R.id.trackrecorderactivity_tab_map_togglerecording_floatingactionbutton)

        this.subscriptions.addAll(
                this.presenter.canStartResumeRecordingChanged.observeOn(AndroidSchedulers.mainThread()).subscribe {
                    var iconId = R.drawable.ic_action_track_recorder_record_startresume
                    if (!it) {
                        iconId = R.drawable.ic_action_track_recorder_recording_pause
                    }

                    toggleRecordingFloatingActionButton.setImageIcon(Icon.createWithResource(this.context, iconId))
                },

                toggleRecordingFloatingActionButton.clicks().subscribe {
                    this.presenter.canStartResumeRecordingChanged.first(false).subscribe {
                        isGranted ->
                            if (isGranted) {
                               this.presenter.startResumeRecording()
                            } else {
                                this.presenter.pauseRecording()
                            }
                    }
                }
        )
    }

    public override fun onStop() {
        super.onStop()

        this.subscriptions.clear()
        this.currentLocationsChangedSubscription?.dispose()
    }

    public override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if(this.activity is TrackRecorderActivity) {
            this.presenter = (this.activity!! as TrackRecorderActivity).presenter
        }
    }

    public override fun onMapReady(trackRecorderMap: ITrackRecorderMap) {
        this.viewHolder.store(trackRecorderMap::class.java.simpleName!!, trackRecorderMap)

        trackRecorderMap.zoomToLocation(LatLng(50.8357, 12.92922), 12f)
    }

    public override fun onMapLoaded(trackRecorderMap: ITrackRecorderMap) {
        this.subscribeToMap()
    }

    private fun subscribeToMap() {
        val trackRecorderMap = this.viewHolder.retrieve<ITrackRecorderMap>(TrackRecorderMapFragment::class.java.simpleName!!)

        this.subscriptions.addAll(
                this.presenter.trackSessionStateChanged.observeOn(AndroidSchedulers.mainThread()).subscribe(trackRecorderMap.consumeReset()),

                this.presenter.locationsChangedAvailable.observeOn(AndroidSchedulers.mainThread()).subscribe {
                    this.currentLocationsChangedSubscription?.dispose()

                    this.currentLocationsChangedSubscription = it
                            .buffer(5, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(trackRecorderMap.consumeLocations())
                }
        )
    }
}