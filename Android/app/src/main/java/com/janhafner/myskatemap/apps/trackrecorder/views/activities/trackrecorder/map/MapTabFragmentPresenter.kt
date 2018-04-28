package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.map

import com.janhafner.myskatemap.apps.trackrecorder.consumeLocations
import com.janhafner.myskatemap.apps.trackrecorder.consumeReset
import com.janhafner.myskatemap.apps.trackrecorder.location.ITrackRecordingSession
import com.janhafner.myskatemap.apps.trackrecorder.location.SimpleLocation
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.ServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.views.map.OnTrackRecorderMapReadyCallback
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

internal final class MapTabFragmentPresenter(private val mapTabFragment: MapTabFragment,
                                             private val trackRecorderServiceController: ServiceController<TrackRecorderServiceBinder>)
    : OnTrackRecorderMapReadyCallback {
    private val trackRecorderServiceControllerSubscription: Disposable

    private var trackRecorderSession: ITrackRecordingSession? = null

    private val sessionSubscriptions: CompositeDisposable = CompositeDisposable()

    init {
        this.trackRecorderServiceControllerSubscription = this.trackRecorderServiceController.startAndBindService().subscribe{
            if(it) {
                val binder = this.trackRecorderServiceController.currentBinder!!

                this.trackRecorderSession = this.getInitializedSession(binder.currentSession!!)

                this.mapTabFragment.trackRecorderMapFragment.getMapAsync(this)
            } else {
                this.uninitializeSession()
            }
        }
    }

    private fun getInitializedSession(trackRecorderSession: ITrackRecordingSession): ITrackRecordingSession {
        this.sessionSubscriptions.addAll(
            trackRecorderSession.stateChanged
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(mapTabFragment.trackRecorderMapFragment.consumeReset()),

            trackRecorderSession.locationsChanged
                    .buffer(1, TimeUnit.SECONDS)
                    .filter{
                        it.any()
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(mapTabFragment.trackRecorderMapFragment.consumeLocations())
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
        this.trackRecorderServiceControllerSubscription.dispose()

        this.uninitializeSession()
    }
}