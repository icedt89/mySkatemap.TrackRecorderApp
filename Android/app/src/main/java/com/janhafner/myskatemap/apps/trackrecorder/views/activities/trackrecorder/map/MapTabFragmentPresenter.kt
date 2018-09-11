package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.map

import android.graphics.Bitmap
import com.janhafner.myskatemap.apps.trackrecorder.BuildConfig
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.common.SimpleLocation
import com.janhafner.myskatemap.apps.trackrecorder.common.filterNotEmpty
import com.janhafner.myskatemap.apps.trackrecorder.services.toSimpleLocation
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.IServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session.ITrackRecordingSession
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session.TrackRecordingSessionState
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.views.INeedFragmentVisibilityInfo
import com.janhafner.myskatemap.apps.trackrecorder.views.map.ITrackRecorderMapFragmentFactory
import com.janhafner.myskatemap.apps.trackrecorder.views.map.OnMapSnapshotReadyCallback
import com.janhafner.myskatemap.apps.trackrecorder.views.map.OnTrackRecorderMapReadyCallback
import com.janhafner.myskatemap.apps.trackrecorder.views.map.TrackRecorderMapFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

internal final class MapTabFragmentPresenter(private val view: MapTabFragment,
                                             private val trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>,
                                             private val trackRecorderMapFragmentFactory: ITrackRecorderMapFragmentFactory,
                                             private val appSettings: IAppSettings)
    : OnTrackRecorderMapReadyCallback, OnMapSnapshotReadyCallback {
    private val subscriptions: CompositeDisposable = CompositeDisposable()

    private val clientSubscriptions: CompositeDisposable = CompositeDisposable()

    private val fragmentSubscriptions: CompositeDisposable = CompositeDisposable()

    private val sessionSubscriptions: CompositeDisposable = CompositeDisposable()

    private var trackRecorderSession: ITrackRecordingSession? = null

    private var trackRecorderMapFragment: TrackRecorderMapFragment? = null

    init {
        val trackRecorderMapFragment = this.trackRecorderMapFragmentFactory.createFragment()
        this.setupMapFragment(trackRecorderMapFragment)

        this.subscriptions.addAll(
                this.appSettings.propertyChanged
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            if (it.hasChanged) {
                                if (it.propertyName == IAppSettings::mapControlTypeName.name) {
                                    //                                  val mapFragment = this.trackRecorderMapFragmentFactory.createFragment()

//                                    this.setupMapFragment(mapFragment)
                                }
                            }
                        }
        )
    }

    private fun setupMapFragment(trackRecorderMapFragment: TrackRecorderMapFragment) {
        // Iam aware that using commitAllowingStateLoss() should only be used as a last restort!
        // But I don`t know how to handle the fragment replacement any other.
        this.view.childFragmentManager.beginTransaction()
                .replace(R.id.fragment_track_recorder_map_map_placeholder, trackRecorderMapFragment)
                .runOnCommit({
                    this.fragmentSubscriptions.addAll(
                            this.trackRecorderServiceController.isClientBoundChanged
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe {
                                        if (it) {
                                            this.clientSubscriptions.add(
                                                    this.trackRecorderServiceController.currentBinder!!.hasCurrentSessionChanged
                                                            .observeOn(AndroidSchedulers.mainThread())
                                                            .subscribe {
                                                                if (it) {
                                                                    val binder = this.trackRecorderServiceController.currentBinder!!

                                                                    this.trackRecorderMapFragment!!.getMapAsync(this)

                                                                    this.trackRecorderSession = this.getInitializedSession(binder.currentSession!!)
                                                                } else {
                                                                    this.uninitializeSession()
                                                                }
                                                            }
                                            )
                                        } else {
                                            this.uninitializeSession()

                                            this.clientSubscriptions.clear()
                                        }
                                    }
                    )
                })
                .commitAllowingStateLoss() // <---- Evil!

        this.trackRecorderMapFragment = trackRecorderMapFragment
    }

    public fun setUserVisibleHint(isVisibleToUser: Boolean) {
        if (this.view.activity is INeedFragmentVisibilityInfo) {
            (this.view.activity as INeedFragmentVisibilityInfo).onFragmentVisibilityChange(this.view, isVisibleToUser)
        }
    }

    private fun getInitializedSession(trackRecorderSession: ITrackRecordingSession): ITrackRecordingSession {
        this.sessionSubscriptions.addAll(
                trackRecorderSession.locationsChanged
                        .map {
                            it.toSimpleLocation()
                        }
                        .buffer(1, TimeUnit.SECONDS)
                        .filterNotEmpty()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            this.trackRecorderMapFragment!!.addLocations(it)
                        },
                trackRecorderSession.stateChanged
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe{
                            this.trackRecorderMapFragment!!.gesturesEnabled = it.state != TrackRecordingSessionState.Running
                        }
        )

        return trackRecorderSession
    }

    private fun uninitializeSession(clearTrack: Boolean = true) {
        this.sessionSubscriptions.clear()

        if (clearTrack && this.trackRecorderMapFragment != null && this.trackRecorderMapFragment!!.isReady) {
            this.trackRecorderMapFragment!!.clearTrack()
        }

        this.trackRecorderSession = null
    }

    public override fun onMapReady(trackRecorderMap: com.janhafner.myskatemap.apps.trackrecorder.views.map.ITrackRecorderMap) {
        trackRecorderMap.zoomToLocation(SimpleLocation(BuildConfig.MAP_INITIAL_LATITUDE, BuildConfig.MAP_INITIAL_LONGITUDE), BuildConfig.MAP_INITIAL_ZOOM)
    }

    public override fun onSnapshotReady(bitmap: Bitmap) {
        bitmap.recycle()
    }

    public fun destroy() {
        this.uninitializeSession(false)

        this.sessionSubscriptions.dispose()
        this.fragmentSubscriptions.dispose()
        this.clientSubscriptions.dispose()
        this.subscriptions.dispose()
    }
}