package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.map

import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.consumeLocations
import com.janhafner.myskatemap.apps.trackrecorder.consumeReset
import com.janhafner.myskatemap.apps.trackrecorder.dropLocationsNotInDistance
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.ITrackRecordingSession
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.SimpleLocation
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.ServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.views.INeedFragmentVisibilityInfo
import com.janhafner.myskatemap.apps.trackrecorder.views.map.ITrackRecorderMapFragmentFactory
import com.janhafner.myskatemap.apps.trackrecorder.views.map.OnTrackRecorderMapReadyCallback
import com.janhafner.myskatemap.apps.trackrecorder.views.map.TrackRecorderMapFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

internal final class MapTabFragmentPresenter(private val mapTabFragment: MapTabFragment,
                                             private val trackRecorderServiceController: ServiceController<TrackRecorderServiceBinder>,
                                             private val trackRecorderMapFragmentFactory: ITrackRecorderMapFragmentFactory)
    : OnTrackRecorderMapReadyCallback {
    private val trackRecorderServiceControllerSubscription: Disposable

    private var trackRecorderSession: ITrackRecordingSession? = null

    private val sessionSubscriptions: CompositeDisposable = CompositeDisposable()

    private var trackRecorderMapFragment: TrackRecorderMapFragment

    init {
        this.trackRecorderMapFragment = this.trackRecorderMapFragmentFactory.getFragment()

        this.mapTabFragment.childFragmentManager.beginTransaction()
                .replace(R.id.fragment_track_recorder_map_map_placeholder, this.trackRecorderMapFragment)
                .commit()

        this.trackRecorderServiceControllerSubscription = this.trackRecorderServiceController.startAndBindService().subscribe{
            if(it) {
                val binder = this.trackRecorderServiceController.currentBinder!!

                this.trackRecorderSession = this.getInitializedSession(binder.currentSession!!)

                this.trackRecorderMapFragment.getMapAsync(this)
            } else {
                this.uninitializeSession()
            }
        }
    }


    public fun setUserVisibleHint(isVisibleToUser: Boolean) {
        if(this.mapTabFragment.activity is INeedFragmentVisibilityInfo) {
            (this.mapTabFragment.activity as INeedFragmentVisibilityInfo).onFragmentVisibilityChange(this.mapTabFragment, isVisibleToUser)
        }
    }

    private fun getInitializedSession(trackRecorderSession: ITrackRecordingSession): ITrackRecordingSession {
        this.sessionSubscriptions.addAll(
            trackRecorderSession.stateChanged
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(trackRecorderMapFragment.consumeReset()),

            trackRecorderSession.locationsChanged
                    .dropLocationsNotInDistance(500.0)
                    .buffer(1, TimeUnit.SECONDS)
                    .filter{
                        it.any()
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(trackRecorderMapFragment.consumeLocations())
        )

        return trackRecorderSession
    }

    private fun uninitializeSession() {
        this.sessionSubscriptions.clear()

        this.trackRecorderSession = null
    }

    public override fun onMapReady(trackRecorderMap: com.janhafner.myskatemap.apps.trackrecorder.views.map.ITrackRecorderMap) {
        trackRecorderMap.zoomToLocation(SimpleLocation(50.8357, 12.92922), 1.0f)
    }

    public fun destroy() {
        this.trackRecorderServiceController.unbindService()

        this.trackRecorderServiceControllerSubscription.dispose()

        this.uninitializeSession()
    }
}