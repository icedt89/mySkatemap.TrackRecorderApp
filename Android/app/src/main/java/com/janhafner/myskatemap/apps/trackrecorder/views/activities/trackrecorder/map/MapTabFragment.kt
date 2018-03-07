package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.map

import android.graphics.drawable.Icon
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.model.LatLng
import com.jakewharton.rxbinding2.view.clicks
import com.janhafner.myskatemap.apps.trackrecorder.*
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.ViewHolder
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.settings.AppSettings
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.ITrackRecorderActivityPresenter
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.ShowLocationServicesSnackbar
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.TrackRecorderActivity
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

    @Deprecated("Resolve using Dagger! React to change of flash color and vibrate: set notification properties!")
    private val appSettings: IAppSettings = AppSettings()

    public override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_map_tab, container, false)
    }

    public override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.viewHolder
                .store(view.findViewById(R.id.trackrecorderactivity_tab_map_togglerecording_floatingactionbutton))

        val mapFragment = this.childFragmentManager.findFragmentById(R.id.trackrecorderactivity_tab_map_googlemap) as TrackRecorderMapFragment

        mapFragment.getMapAsync(this)

        this.appSettings.appSettingsChanged.subscribe{
            if(it.propertyName == "trackColor" && it.oldValue != it.newValue) {
                val trackRecorderMap = this.viewHolder.tryRetrieve<ITrackRecorderMap>(ITrackRecorderMap::javaClass.name)
                if(trackRecorderMap != null) {
                    trackRecorderMap.trackColor = it.newValue as Int
                }
            }
        }
    }

    public override fun onDestroy() {
        super.onDestroy()

        this.viewHolder.clear()
    }

    public override fun onStart() {
        super.onStart()

        val toggleRecordingFloatingActionButton = this.viewHolder.retrieve<FloatingActionButton>(R.id.trackrecorderactivity_tab_map_togglerecording_floatingactionbutton)

        val trackRecorderMap = this.viewHolder.tryRetrieve<ITrackRecorderMap>(ITrackRecorderMap::javaClass.name)
        if(trackRecorderMap != null) {
            this.subscribeToMap(trackRecorderMap)
        }

        this.subscriptions.addAll(
                this.presenter.canStartResumeRecordingChanged.observeOn(AndroidSchedulers.mainThread()).subscribe {
                    var iconId = R.drawable.ic_action_track_recorder_recording_startresume
                    if (!it) {
                        iconId = R.drawable.ic_action_track_recorder_recording_pause
                    }

                    toggleRecordingFloatingActionButton.setImageIcon(Icon.createWithResource(this.context, iconId))
                },

                toggleRecordingFloatingActionButton.clicks().subscribe {
                    this.presenter.canStartResumeRecordingChanged.first(false).subscribe {
                        isGranted ->
                            if (isGranted) {
                                if(this.context!!.isLocationServicesEnabled()) {
                                    this.presenter.startResumeRecording()
                                }else{
                                    ShowLocationServicesSnackbar.make(this.activity!!, this.view!!).show()
                                }
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
        this.viewHolder.store(trackRecorderMap::javaClass.name, trackRecorderMap)

        trackRecorderMap.zoomToLocation(LatLng(50.8357, 12.92922), 12f)
    }

    public override fun onMapLoaded(trackRecorderMap: ITrackRecorderMap) {
        this.subscribeToMap(trackRecorderMap)
    }

    private fun subscribeToMap(trackRecorderMap: ITrackRecorderMap) {
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