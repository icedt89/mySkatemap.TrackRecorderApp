package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.map

import android.graphics.drawable.Icon
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding2.view.clicks
import com.janhafner.myskatemap.apps.trackrecorder.*
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.ViewHolder
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.location.SimpleLocation
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.ITrackRecorderActivityPresenter
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.ShowLocationServicesSnackbar
import com.janhafner.myskatemap.apps.trackrecorder.views.map.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit
import javax.inject.Inject


internal final class MapTabFragment: Fragment(), OnTrackRecorderMapReadyCallback, OnTrackRecorderMapLoadedCallback {
    @Inject
    public lateinit var presenter: ITrackRecorderActivityPresenter

    private var currentLocationsChangedSubscription: Disposable? = null

    private val subscriptions: CompositeDisposable = CompositeDisposable()

    private val viewHolder: ViewHolder = ViewHolder()

    @Inject
    public lateinit var appSettings: IAppSettings

    public override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_map_tab, container, false)
    }

    public override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        this.context!!.getApplicationInjector().inject(this)

        super.onViewCreated(view, savedInstanceState)

        this.viewHolder
                .store(view.findViewById(R.id.trackrecorderactivity_tab_map_togglerecording_floatingactionbutton))
    }

    public override fun onDestroy() {
        super.onDestroy()

        this.viewHolder.clear()
    }

    public override fun onStart() {
        super.onStart()

        val mapFragment = this.childFragmentManager.findFragmentById(R.id.trackrecorderactivity_tab_map_map) as ITrackRecorderMapWithDelayedInitialization

        mapFragment.getMapAsync(this)

        val toggleRecordingFloatingActionButton = this.viewHolder.retrieve<FloatingActionButton>(R.id.trackrecorderactivity_tab_map_togglerecording_floatingactionbutton)

        val trackRecorderMap = this.viewHolder.tryRetrieve<com.janhafner.myskatemap.apps.trackrecorder.views.map.ITrackRecorderMap>(Fragment::class.java.name)
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
                                    this.presenter.resumeRecording()
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

    public override fun onMapReady(trackRecorderMap: com.janhafner.myskatemap.apps.trackrecorder.views.map.ITrackRecorderMap) {
        this.viewHolder.store(trackRecorderMap::class.java.name, trackRecorderMap)

        trackRecorderMap.zoomToLocation(SimpleLocation(50.8357, 12.92922), 12f)
    }

    public override fun onMapLoaded(trackRecorderMap: com.janhafner.myskatemap.apps.trackrecorder.views.map.ITrackRecorderMap) {
        this.subscribeToMap(trackRecorderMap)
    }

    private fun subscribeToMap(trackRecorderMap: com.janhafner.myskatemap.apps.trackrecorder.views.map.ITrackRecorderMap) {
        this.subscriptions.addAll(
                this.presenter.trackSessionStateChanged.observeOn(AndroidSchedulers.mainThread()).subscribe(trackRecorderMap.consumeReset()),

                this.presenter.locationsChangedAvailable.observeOn(AndroidSchedulers.mainThread()).subscribe {
                    this.currentLocationsChangedSubscription?.dispose()

                    this.currentLocationsChangedSubscription = it
                            .buffer(1, TimeUnit.SECONDS)
                            .filter {
                                it.any()
                            }
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(trackRecorderMap.consumeLocations())
                }
        )
    }
}