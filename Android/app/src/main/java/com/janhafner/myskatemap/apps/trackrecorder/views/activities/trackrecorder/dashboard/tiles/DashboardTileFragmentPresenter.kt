package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles

import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.IServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.ITrackRecordingSession
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

internal abstract class DashboardTileFragmentPresenter(protected val view: DashboardTileFragment,
                                                       protected val appSettings: IAppSettings,
                                                       protected val trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>) {
    private val trackRecorderServiceControllerSubscription: Disposable

    private var sessionAvailabilityChangedSubscription: Disposable? = null

    private var trackRecorderSession: ITrackRecordingSession? = null

    private val sessionSubscriptions: CompositeDisposable = CompositeDisposable()

    init {
        this.initialize()

        this.trackRecorderServiceControllerSubscription = this.trackRecorderServiceController.isClientBoundChanged.subscribe{
            if(it) {
                this.sessionAvailabilityChangedSubscription = this.trackRecorderServiceController.currentBinder!!.hasCurrentSessionChanged.subscribe{
                    if(it) {
                        this.trackRecorderSession = this.getInitializedSession(this.trackRecorderServiceController.currentBinder!!.currentSession!!)
                    } else {
                        this.uninitializeSession()
                    }
                }
            } else {
                this.uninitializeSession()

                this.sessionAvailabilityChangedSubscription?.dispose()
            }
        }
    }

    private fun getInitializedSession(trackRecorderSession: ITrackRecordingSession): ITrackRecordingSession {
        val subscriptions = this.createSubscriptions(trackRecorderSession)
        for (subscription in subscriptions) {
            this.sessionSubscriptions.addAll(subscription)
        }

        return trackRecorderSession
    }

    protected abstract fun initialize()

    protected abstract fun createSubscriptions(trackRecorderSession: ITrackRecordingSession): List<Disposable>

    private fun uninitializeSession() {
        this.sessionSubscriptions.clear()

        this.trackRecorderSession = null

        this.resetView()
    }

    protected abstract fun resetView()

    public fun destroy() {
        this.trackRecorderServiceController.unbindService()

        this.trackRecorderServiceControllerSubscription.dispose()
        this.sessionAvailabilityChangedSubscription?.dispose()

        this.uninitializeSession()

        this.sessionSubscriptions.dispose()
    }
}