package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.map

import android.view.View
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.view.visibility
import com.janhafner.myskatemap.apps.trackrecorder.BuildConfig
import com.janhafner.myskatemap.apps.trackrecorder.LocationAvailability
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.common.PropertyChangedData
import com.janhafner.myskatemap.apps.trackrecorder.common.filterNotEmpty
import com.janhafner.myskatemap.apps.trackrecorder.common.hasChanged
import com.janhafner.myskatemap.apps.trackrecorder.common.isNamed
import com.janhafner.myskatemap.apps.trackrecorder.common.types.LocationReceivedActivityStreamItem
import com.janhafner.myskatemap.apps.trackrecorder.common.types.MapActivityStreamItem
import com.janhafner.myskatemap.apps.trackrecorder.common.types.StartNewSegmentActivityStreamItem
import com.janhafner.myskatemap.apps.trackrecorder.map.ITrackRecorderMap
import com.janhafner.myskatemap.apps.trackrecorder.map.MapLocation
import com.janhafner.myskatemap.apps.trackrecorder.map.OnTrackRecorderMapReadyCallback
import com.janhafner.myskatemap.apps.trackrecorder.map.TrackRecorderMapFragment
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.IServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session.ITrackRecordingSession
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session.TrackRecordingSessionState
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.views.INeedFragmentVisibilityInfo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_map_tab.*
import java.util.concurrent.TimeUnit

internal final class MapTabFragmentPresenter(private val view: MapTabFragment,
                                             private val trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>,
                                             private val trackRecorderMapFragment: TrackRecorderMapFragment,
                                             private val appSettings: IAppSettings)
    : OnTrackRecorderMapReadyCallback {
    private val subscriptions: CompositeDisposable = CompositeDisposable()

    private val clientSubscriptions: CompositeDisposable = CompositeDisposable()

    private val fragmentSubscriptions: CompositeDisposable = CompositeDisposable()

    private val sessionSubscriptions: CompositeDisposable = CompositeDisposable()

    private var trackRecorderSession: ITrackRecordingSession? = null

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

    public fun setUserVisibleHint(isVisibleToUser: Boolean) {
        if (isVisibleToUser) {
            this.initialize()
        } else {
            this.uninitialize()
        }

        if (this.view.activity is INeedFragmentVisibilityInfo) {
            (this.view.activity as INeedFragmentVisibilityInfo).onFragmentVisibilityChange(this.view, isVisibleToUser)
        }
    }

    private fun getInitializedSession(trackRecorderSession: ITrackRecordingSession): ITrackRecordingSession {
        // TODO: Rapid FAB: https://github.com/wangjiegulu/RapidFloatingActionButton
        this.sessionSubscriptions.addAll(
                trackRecorderSession.mapActivityStream
                        .subscribeOn(Schedulers.computation())
                        .buffer(250, TimeUnit.MILLISECONDS, 500)
                        .filterNotEmpty()
                        .map {
                            val result = mutableListOf<() -> Unit>()

                            val items = mutableListOf<LocationReceivedActivityStreamItem>()
                            for (item in it) {
                                if (item is LocationReceivedActivityStreamItem) {
                                    items.add(item)
                                } else if (item is StartNewSegmentActivityStreamItem) {
                                    if(!items.any()) {
                                        continue
                                    }

                                    val closureItems = items.map {
                                        MapLocation(it.latitude, it.longitude)
                                    }
                                    result.add {
                                        this.trackRecorderMapFragment.addLocations(closureItems)
                                    }

                                    items.clear()

                                    result.add {
                                        this.trackRecorderMapFragment.beginNewTrackSegment()
                                    }
                                }
                            }

                            val closureItems = items.map {
                                MapLocation(it.latitude, it.longitude)
                            }
                            result.add {
                                this.trackRecorderMapFragment.addLocations(closureItems)
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
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            this.trackRecorderMapFragment.gesturesEnabled = it.state != TrackRecordingSessionState.Running
                        },
                this.view.fragment_track_recorder_map_add_poi.clicks()
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .subscribe {
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
        this.subscriptions.addAll(
                this.appSettings.propertyChanged
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
                        },
                LocationAvailability.changed(this.view.context!!)
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .distinctUntilChanged()
                        .subscribe {
                            var visibility = View.VISIBLE
                            if (!it) {
                                visibility = View.GONE
                            }

                            this.view.fragment_track_recorder_map_add_poi.visibility = visibility
                        },
                this.trackRecorderServiceController.isClientBoundChanged
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            if (it) {
                                this.clientSubscriptions.addAll(
                                        this.trackRecorderServiceController.currentBinder!!.hasCurrentSessionChanged
                                                .subscribeOn(Schedulers.computation())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe {
                                                    if (it) {
                                                        val binder = this.trackRecorderServiceController.currentBinder!!

                                                        this.trackRecorderSession = this.getInitializedSession(binder.currentSession!!)
                                                    } else {
                                                        this.uninitializeSession()
                                                    }
                                                },
                                        this.trackRecorderServiceController.currentBinder!!.hasCurrentSessionChanged
                                                .subscribeOn(Schedulers.computation())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(this.view.fragment_track_recorder_map_add_poi.visibility())
                                )
                            } else {
                                this.uninitializeSession()

                                this.clientSubscriptions.clear()
                            }
                        }
        )
    }

    private fun uninitialize() {
        this.uninitializeSession(true)

        this.sessionSubscriptions.clear()
        this.fragmentSubscriptions.clear()
        this.clientSubscriptions.clear()
        this.subscriptions.clear()
    }
}