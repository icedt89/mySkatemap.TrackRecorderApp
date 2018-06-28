package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.map

import android.graphics.Bitmap
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.SimpleLocation
import com.janhafner.myskatemap.apps.trackrecorder.consumeLocations
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.ITrackRecordingSession
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.IServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.views.INeedFragmentVisibilityInfo
import com.janhafner.myskatemap.apps.trackrecorder.views.map.ITrackRecorderMapFragmentFactory
import com.janhafner.myskatemap.apps.trackrecorder.views.map.OnMapSnapshotReadyCallback
import com.janhafner.myskatemap.apps.trackrecorder.views.map.OnTrackRecorderMapReadyCallback
import com.janhafner.myskatemap.apps.trackrecorder.views.map.TrackRecorderMapFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

internal final class MapTabFragmentPresenter(private val view: MapTabFragment,
                                             private val trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>,
                                             private val trackRecorderMapFragmentFactory: ITrackRecorderMapFragmentFactory)
    : OnTrackRecorderMapReadyCallback, OnMapSnapshotReadyCallback {
    private lateinit var trackRecorderServiceControllerSubscription: Disposable

    private var sessionAvailabilityChangedSubscription: Disposable? = null

    private var trackRecorderSession: ITrackRecordingSession? = null

    private val sessionSubscriptions: CompositeDisposable = CompositeDisposable()

    private var trackRecorderMapFragment: TrackRecorderMapFragment = this.trackRecorderMapFragmentFactory.getFragment()

    init {
        this.view.childFragmentManager.beginTransaction()
                .replace(R.id.fragment_track_recorder_map_map_placeholder, this.trackRecorderMapFragment)
                .runOnCommit({
                    this.trackRecorderServiceControllerSubscription = this.trackRecorderServiceController.isClientBoundChanged.subscribe{
                        if(it) {
                            this.sessionAvailabilityChangedSubscription = this.trackRecorderServiceController.currentBinder!!.hasCurrentSessionChanged.subscribe{
                                if(it) {
                                    val binder = this.trackRecorderServiceController.currentBinder!!

                                    this.trackRecorderMapFragment.getMapAsync(this)

                                    this.trackRecorderSession = this.getInitializedSession(binder.currentSession!!)
                                } else {
                                    this.uninitializeSession()
                                }
                            }
                        } else {
                            this.uninitializeSession()

                            this.sessionAvailabilityChangedSubscription?.dispose()
                        }
                    }
                })
                .commit()
    }

    public fun setUserVisibleHint(isVisibleToUser: Boolean) {
        if(this.view.activity is INeedFragmentVisibilityInfo) {
            (this.view.activity as INeedFragmentVisibilityInfo).onFragmentVisibilityChange(this.view, isVisibleToUser)
        }
    }

    private fun getInitializedSession(trackRecorderSession: ITrackRecordingSession): ITrackRecordingSession {
        this.sessionSubscriptions.addAll(
            trackRecorderSession.locationsChanged
                    .buffer(1, TimeUnit.SECONDS)
                    .filter{
                        it.any()
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(trackRecorderMapFragment.consumeLocations()),

            trackRecorderSession.recordingSaved
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe{
                        // TODO
                        // this.trackRecorderMapFragment.getSnapshotAsync(this)
                    }
        )

        return trackRecorderSession
    }

    private fun uninitializeSession() {
        this.sessionSubscriptions.clear()

        if(this.trackRecorderMapFragment.isReady) {
            this.trackRecorderMapFragment.clearTrack()
        }

        this.trackRecorderSession = null
    }

    public override fun onMapReady(trackRecorderMap: com.janhafner.myskatemap.apps.trackrecorder.views.map.ITrackRecorderMap) {
        trackRecorderMap.zoomToLocation(SimpleLocation(50.8357, 12.92922), 1.0f)
    }

    public override fun onSnapshotReady(bitmap: Bitmap) {
        bitmap.recycle()
    }

    public fun destroy() {
        this.trackRecorderServiceController.unbindService()
        this.trackRecorderServiceControllerSubscription.dispose()

        this.sessionAvailabilityChangedSubscription?.dispose()

        this.uninitializeSession()

        this.sessionSubscriptions.dispose()
    }
}