package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.map

import android.view.View
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.view.visibility
import com.janhafner.myskatemap.apps.trackrecorder.BuildConfig
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.common.filterNotEmpty
import com.janhafner.myskatemap.apps.trackrecorder.common.toSimpleLocation
import com.janhafner.myskatemap.apps.trackrecorder.common.types.SimpleLocation
import com.janhafner.myskatemap.apps.trackrecorder.locationavailability.ILocationAvailabilityChangedSource
import com.janhafner.myskatemap.apps.trackrecorder.map.ITrackRecorderMap
import com.janhafner.myskatemap.apps.trackrecorder.map.OnTrackRecorderMapReadyCallback
import com.janhafner.myskatemap.apps.trackrecorder.map.TrackRecorderMapFragment
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.IServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider.IMyLocationProvider
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider.IMyLocationRequestState
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session.ITrackRecordingSession
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session.TrackRecordingSessionState
import com.janhafner.myskatemap.apps.trackrecorder.views.INeedFragmentVisibilityInfo
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_map_tab.*
import java.util.concurrent.TimeUnit

internal final class MapTabFragmentPresenter(private val view: MapTabFragment,
                                             private val trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>,
                                             private val trackRecorderMapFragment: TrackRecorderMapFragment,
                                             private val myLocationProvider: IMyLocationProvider,
                                             private val locationAvailabilityChangedSource: ILocationAvailabilityChangedSource)
    : OnTrackRecorderMapReadyCallback {
    private val subscriptions: CompositeDisposable = CompositeDisposable()

    private val clientSubscriptions: CompositeDisposable = CompositeDisposable()

    private val fragmentSubscriptions: CompositeDisposable = CompositeDisposable()

    private val sessionSubscriptions: CompositeDisposable = CompositeDisposable()

    private var trackRecorderSession: ITrackRecordingSession? = null

    private var currentMyLocationRequestState: IMyLocationRequestState? = null

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
        if (this.view.activity is INeedFragmentVisibilityInfo) {
            (this.view.activity as INeedFragmentVisibilityInfo).onFragmentVisibilityChange(this.view, isVisibleToUser)
        }
    }

    private fun getInitializedSession(trackRecorderSession: ITrackRecordingSession): ITrackRecordingSession {
        // TODO: Rapid FAB: https://github.com/wangjiegulu/RapidFloatingActionButton
        this.sessionSubscriptions.addAll(
                trackRecorderSession.locationsChanged
                        .map {
                            it.toSimpleLocation()
                        }
                        .buffer(250, TimeUnit.MILLISECONDS, 250)
                        .filterNotEmpty()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            this.trackRecorderMapFragment.addLocations(it)
                        },
                trackRecorderSession.stateChanged
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe{
                            this.trackRecorderMapFragment.gesturesEnabled = it.state != TrackRecordingSessionState.Running
                        },
                this.view.fragment_track_recorder_map_add_poi.clicks().subscribe {
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

        trackRecorderMap.zoomToLocation(SimpleLocation(BuildConfig.MAP_INITIAL_LATITUDE, BuildConfig.MAP_INITIAL_LONGITUDE), BuildConfig.MAP_INITIAL_ZOOM)
    }

    public fun destroy() {
        this.uninitializeSession(false)

        this.tryCancelCurrentMyLocationRequest()

        this.sessionSubscriptions.dispose()
        this.fragmentSubscriptions.dispose()
        this.clientSubscriptions.dispose()
        this.subscriptions.dispose()
    }

    public fun onResume() {
        this.subscriptions.addAll(
                this.view.fragment_track_recorder_map_fix_myposition.clicks().subscribe {
                    if (this.currentMyLocationRequestState == null) {
                        val currentLocationRequest = this.myLocationProvider.getMyCurrentLocation()
                        currentLocationRequest.location.observeOn(AndroidSchedulers.mainThread())
                                .subscribe {
                                    result ->
                                    if(result.value != null) {
                                        this.trackRecorderMapFragment.zoomToLocation(result.value!!.toSimpleLocation(), 18.0f)

                                        if(this.trackRecorderMapFragment.canAddMarker) {
                                            val mapMarkerToken = this.trackRecorderMapFragment.addMarker(result.value!!.toSimpleLocation(), this.view.getString(R.string.trackrecorderactivity_map_mycurrentlocation_marker_title))
                                            Single.just(mapMarkerToken)
                                                    .delay(10, TimeUnit.SECONDS)
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe {
                                                        emitter ->
                                                        emitter.destroy()
                                                    }
                                        }
                                    }
                                }

                        this.currentMyLocationRequestState = currentLocationRequest
                    } else {
                        this.tryCancelCurrentMyLocationRequest()
                    }
                },
                this.locationAvailabilityChangedSource.locationAvailable
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe{
                            var visibility = View.VISIBLE
                            if(!it) {
                                visibility = View.GONE
                            }

                            this.view.fragment_track_recorder_map_add_poi.visibility = visibility
                            this.view.fragment_track_recorder_map_fix_myposition.visibility = visibility
                        },
                this.trackRecorderServiceController.isClientBoundChanged
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            if (it) {
                                this.clientSubscriptions.addAll(
                                        this.trackRecorderServiceController.currentBinder!!.hasCurrentSessionChanged
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

    public fun onPause() {
        this.uninitializeSession(true)

        this.tryCancelCurrentMyLocationRequest()

        this.sessionSubscriptions.clear()
        this.fragmentSubscriptions.clear()
        this.clientSubscriptions.clear()
        this.subscriptions.clear()
    }

    private fun tryCancelCurrentMyLocationRequest() {
        if (this.currentMyLocationRequestState != null) {
            this.currentMyLocationRequestState!!.cancel()

            this.currentMyLocationRequestState = null
        }
    }
}