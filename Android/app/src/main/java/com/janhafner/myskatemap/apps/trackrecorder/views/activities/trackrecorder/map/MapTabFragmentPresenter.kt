package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.map

import androidx.lifecycle.Lifecycle
import com.janhafner.myskatemap.apps.trackrecorder.BuildConfig
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.core.PropertyChangedData
import com.janhafner.myskatemap.apps.trackrecorder.core.filterNotEmpty
import com.janhafner.myskatemap.apps.trackrecorder.core.hasChanged
import com.janhafner.myskatemap.apps.trackrecorder.core.isNamed
import com.janhafner.myskatemap.apps.trackrecorder.core.types.Location
import com.janhafner.myskatemap.apps.trackrecorder.findChildFragmentById
import com.janhafner.myskatemap.apps.trackrecorder.map.ITrackRecorderMap
import com.janhafner.myskatemap.apps.trackrecorder.map.OnTrackRecorderMapReadyCallback
import com.janhafner.myskatemap.apps.trackrecorder.map.TrackRecorderMapFragment
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.IServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session.ITrackRecordingSession
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session.TrackRecordingSessionState
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import com.uber.autodispose.AutoDispose
import com.uber.autodispose.AutoDispose.autoDisposable
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_trackrecorderactivity_map_tab.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

internal final class MapTabFragmentPresenter(private val view: MapTabFragment,
                                             private val trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>,
                                             private val appSettings: IAppSettings,
                                             private val mustSegmentTrack: Boolean)
    : OnTrackRecorderMapReadyCallback {
    private val sessionSubscriptions: CompositeDisposable = CompositeDisposable()

    private var trackRecorderSession: ITrackRecordingSession? = null

    private val currentSegmentNumber = AtomicInteger()

    private val trackRecorderMapFragment: TrackRecorderMapFragment = this.view.findChildFragmentById(R.id.fragment_track_recorder_map)

    init {
        this.initialize()
    }

    private fun getInitializedSession(trackRecorderSession: ITrackRecordingSession): ITrackRecordingSession {
        this.sessionSubscriptions.addAll(
                trackRecorderSession.locationsChanged
                        .subscribeOn(Schedulers.computation())
                        .doOnSubscribe {
                            this.currentSegmentNumber.set(0)
                        }
                        .buffer(50, TimeUnit.MILLISECONDS, 250)
                        .filterNotEmpty()
                        .map {
                            val result = mutableListOf<() -> Unit>()

                            if(this.mustSegmentTrack) {
                                for (group in it.sortedBy { it.segmentNumber }.groupBy { it.segmentNumber }) {
                                    if (group.key > this.currentSegmentNumber.get()) {
                                        result.add {
                                            this.trackRecorderMapFragment.beginNewTrackSegment()
                                        }

                                        this.currentSegmentNumber.set(group.key)
                                    }

                                    val closureItems = group.value
                                    result.add {
                                        this.trackRecorderMapFragment.addLocations(closureItems)
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
                        .`as`(autoDisposable(AndroidLifecycleScopeProvider.from(this.view, Lifecycle.Event.ON_DESTROY)))
                        .subscribe {
                            for (action in it) {
                                action()
                            }

                            this.trackRecorderMapFragment.focusTrack()
                        },
                trackRecorderSession.stateChanged
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .`as`(autoDisposable(AndroidLifecycleScopeProvider.from(this.view, Lifecycle.Event.ON_DESTROY)))
                        .subscribe {
                            this.trackRecorderMapFragment.gesturesEnabled = it.state != TrackRecordingSessionState.Running
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

        trackRecorderMap.zoomToLocation(Location.simple(BuildConfig.MAP_INITIAL_LATITUDE, BuildConfig.MAP_INITIAL_LONGITUDE), BuildConfig.MAP_INITIAL_ZOOM)
    }

    private fun initialize() {
        this.trackRecorderMapFragment.getMapAsync(this)

        this.view.fragment_track_recorder_map_show_my_position_on_map.isChecked = this.appSettings.showMyLocation
        this.view.fragment_track_recorder_map_show_positions.isChecked = this.appSettings.showPositionsOnMap

        this.view.fragment_track_recorder_map_show_my_position_on_map
                .checkedChanged
                .`as`(autoDisposable(AndroidLifecycleScopeProvider.from(this.view, Lifecycle.Event.ON_DESTROY)))
                .subscribe {
                    this.appSettings.showMyLocation = it
                }
        this.view.fragment_track_recorder_map_show_positions
                .checkedChanged
                .`as`(autoDisposable(AndroidLifecycleScopeProvider.from(this.view, Lifecycle.Event.ON_DESTROY)))
                .subscribe {
                    this.appSettings.showPositionsOnMap = it
                }
        this.appSettings.propertyChanged
                .subscribeOn(Schedulers.computation())
                .hasChanged()
                .isNamed(IAppSettings::showMyLocation.name)
                .startWith(PropertyChangedData(IAppSettings::showMyLocation.name, null, this.trackRecorderMapFragment.providesNativeMyLocation && this.appSettings.showMyLocation))
                .map {
                    this.trackRecorderMapFragment.providesNativeMyLocation && it.newValue as Boolean
                }
                .observeOn(AndroidSchedulers.mainThread())
                .`as`(autoDisposable(AndroidLifecycleScopeProvider.from(this.view, Lifecycle.Event.ON_DESTROY)))
                .subscribe {
                    this.trackRecorderMapFragment.myLocationActivated = it
                }
        this.appSettings.propertyChanged
                .subscribeOn(Schedulers.computation())
                .hasChanged()
                .isNamed(IAppSettings::showPositionsOnMap.name)
                .startWith(PropertyChangedData(IAppSettings::showPositionsOnMap.name, null, this.appSettings.showPositionsOnMap))
                .map {
                    it.newValue as Boolean
                }
                .observeOn(AndroidSchedulers.mainThread())
                .`as`(autoDisposable(AndroidLifecycleScopeProvider.from(this.view, Lifecycle.Event.ON_DESTROY)))
                .subscribe {
                    this.trackRecorderMapFragment.showPositions = it
                }
        this.appSettings.propertyChanged
                .subscribeOn(Schedulers.computation())
                .hasChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .`as`(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this.view, Lifecycle.Event.ON_DESTROY)))
                .subscribe {
                    when (it.propertyName) {
                        IAppSettings::showMyLocation.name -> {
                            this.view.fragment_track_recorder_map_show_my_position_on_map.isChecked = it.newValue as Boolean
                        }
                        IAppSettings::showPositionsOnMap.name -> {
                            this.view.fragment_track_recorder_map_show_positions.isChecked = it.newValue as Boolean
                        }
                    }
                }
        this.trackRecorderServiceController.isClientBoundChanged
                .subscribeOn(Schedulers.computation())
                .flatMap {
                    if (it) {
                        this.trackRecorderServiceController.currentBinder!!.hasCurrentSessionChanged
                    } else {
                        Observable.just(false)
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .`as`(autoDisposable(AndroidLifecycleScopeProvider.from(this.view, Lifecycle.Event.ON_DESTROY)))
                .subscribe {
                    if (it) {
                        val binder = this.trackRecorderServiceController.currentBinder!!

                        this.trackRecorderSession = this.getInitializedSession(binder.currentSession!!)
                    } else {
                        this.uninitializeSession()
                    }
                }
    }
}