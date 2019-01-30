package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.map

import android.view.View
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.view.visibility
import com.janhafner.myskatemap.apps.trackrecorder.BuildConfig
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.common.*
import com.janhafner.myskatemap.apps.trackrecorder.locationServicesAvailabilityChanged
import com.janhafner.myskatemap.apps.trackrecorder.map.ITrackRecorderMap
import com.janhafner.myskatemap.apps.trackrecorder.map.MapLocation
import com.janhafner.myskatemap.apps.trackrecorder.map.OnTrackRecorderMapReadyCallback
import com.janhafner.myskatemap.apps.trackrecorder.map.TrackRecorderMapFragment
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.IServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session.ITrackRecordingSession
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session.TrackRecordingSessionState
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.toMapLocation
import com.trello.rxlifecycle3.android.lifecycle.kotlin.bindUntilEvent
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_trackrecorderactivity_map_tab.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

internal final class MapTabFragmentPresenter(private val view: MapTabFragment,
                                             private val trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>,
                                             private val trackRecorderMapFragment: TrackRecorderMapFragment,
                                             private val appSettings: IAppSettings)
    : OnTrackRecorderMapReadyCallback {
    private val sessionSubscriptions: CompositeDisposable = CompositeDisposable()

    private var trackRecorderSession: ITrackRecordingSession? = null

    private val currentSegmentNumber = AtomicInteger()

    init {
        this.setupMapFragment(this.trackRecorderMapFragment)

        this.initialize()
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

    private fun getInitializedSession(trackRecorderSession: ITrackRecordingSession): ITrackRecordingSession {
        // TODO: Rapid FAB: https://github.com/wangjiegulu/RapidFloatingActionButton
        this.sessionSubscriptions.addAll(
                trackRecorderSession.locationsChanged
                        .bindUntilEvent(this.view, Lifecycle.Event.ON_DESTROY)
                        .subscribeOn(Schedulers.computation())
                        .doOnSubscribe {
                            this.currentSegmentNumber.set(0)
                        }
                        .buffer(50, TimeUnit.MILLISECONDS, 250)
                        .filterNotEmpty()
                        .map {
                            val result = mutableListOf<() -> Unit>()

                            for (group in it.sortedBy { it.segmentNumber }.groupBy { it.segmentNumber }) {
                                if (group.key > this.currentSegmentNumber.get()) {
                                    result.add {
                                        this.trackRecorderMapFragment.beginNewTrackSegment()
                                    }

                                    this.currentSegmentNumber.set(group.key)
                                }

                                val closureItems = group.value.map {
                                    it.toMapLocation()
                                }
                                result.add {
                                    this.trackRecorderMapFragment.addLocations(closureItems)
                                }
                            }

                            result
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            for (action in it) {
                                action()
                            }

                            this.trackRecorderMapFragment.focusTrack()
                        },
                trackRecorderSession.stateChanged
                        .bindUntilEvent(this.view, Lifecycle.Event.ON_DESTROY)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            this.trackRecorderMapFragment.gesturesEnabled = it.state != TrackRecordingSessionState.Running
                        },
                this.view.fragment_track_recorder_map_add_poi.clicks()
                        .timestamp()
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            ToastManager.showToast(this.view.context!!, "TEST ${it.time()}", Toast.LENGTH_LONG)
                            // TODO: Add POI TO CURRENT SESSION
                        }
        )

        return trackRecorderSession
    }

    private fun uninitializeSession(clearTrack: Boolean = true) {
        this.sessionSubscriptions.clear()

        if (clearTrack && this.trackRecorderMapFragment.isReady) {
            this.trackRecorderMapFragment.clearTrack()
        }

        this.trackRecorderSession = null
    }

    public override fun onMapReady(trackRecorderMap: ITrackRecorderMap) {
        trackRecorderMap.trackColor = this.view.context!!.getColor(R.color.accentColor)

        trackRecorderMap.zoomToLocation(MapLocation(BuildConfig.MAP_INITIAL_LATITUDE, BuildConfig.MAP_INITIAL_LONGITUDE), BuildConfig.MAP_INITIAL_ZOOM)
    }

    private fun initialize() {
        this.appSettings.propertyChanged
                .bindUntilEvent(this.view, Lifecycle.Event.ON_DESTROY)
                .subscribeOn(Schedulers.computation())
                .hasChanged()
                .isNamed(IAppSettings::showMyLocation.name)
                .startWith(PropertyChangedData(IAppSettings::showMyLocation.name, null, this.trackRecorderMapFragment.providesNativeMyLocation && this.appSettings.showMyLocation))
                .map {
                    this.trackRecorderMapFragment.providesNativeMyLocation && it.newValue as Boolean
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    this.trackRecorderMapFragment.myLocationActivated = it
                }
        this.appSettings.propertyChanged
                .bindUntilEvent(this.view, Lifecycle.Event.ON_DESTROY)
                .subscribeOn(Schedulers.computation())
                .hasChanged()
                .isNamed(IAppSettings::showPositionsOnMap.name)
                .startWith(PropertyChangedData(IAppSettings::showPositionsOnMap.name, null, this.appSettings.showPositionsOnMap))
                .map {
                    it.newValue as Boolean
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    this.trackRecorderMapFragment.showPositions = it
                }
        this.view.context!!.locationServicesAvailabilityChanged()
                .bindUntilEvent(this.view, Lifecycle.Event.ON_DESTROY)
                .onErrorReturn { false }
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    var visibility = View.VISIBLE
                    if (!it) {
                        visibility = View.GONE
                    }

                    this.view.fragment_track_recorder_map_add_poi.visibility = visibility
                }
        this.trackRecorderServiceController.isClientBoundChanged
                .bindUntilEvent(this.view, Lifecycle.Event.ON_DESTROY)
                .subscribeOn(Schedulers.computation())
                .flatMap {
                    if (it) {
                        this.trackRecorderServiceController.currentBinder!!.hasCurrentSessionChanged
                    } else {
                        Observable.just(false)
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (it) {
                        val binder = this.trackRecorderServiceController.currentBinder!!

                        this.trackRecorderSession = this.getInitializedSession(binder.currentSession!!)
                    } else {
                        this.uninitializeSession()
                    }

                    this.view.fragment_track_recorder_map_add_poi.visibility().accept(it)
                }
    }
}