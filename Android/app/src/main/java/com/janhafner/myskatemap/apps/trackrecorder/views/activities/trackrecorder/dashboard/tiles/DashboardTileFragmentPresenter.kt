package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles

import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.IServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.ITrackRecordingSession
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

internal abstract class DashboardTileFragmentPresenter(protected val view: DashboardTileFragment,
                                                       protected val appSettings: IAppSettings,
                                                       protected val trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>) {
    private val subscriptions: CompositeDisposable = CompositeDisposable()

    private val clientSubscriptions: CompositeDisposable = CompositeDisposable()

    private val sessionSubscriptions: CompositeDisposable = CompositeDisposable()

    private var trackRecorderSession: ITrackRecordingSession? = null

    init {
        this.initialize()

        this.subscriptions.addAll(
                this.trackRecorderServiceController.isClientBoundChanged
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            if (it) {
                                this.clientSubscriptions.addAll(
                                        this.trackRecorderServiceController.currentBinder!!.hasCurrentSessionChanged
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe {
                                                    if (it) {
                                                        this.trackRecorderSession = this.getInitializedSession(this.trackRecorderServiceController.currentBinder!!.currentSession!!)
                                                    } else {
                                                        this.uninitializeSession()
                                                    }
                                                })
                            } else {
                                this.uninitializeSession()

                                this.clientSubscriptions.clear()
                            }
                        })
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
        this.uninitializeSession()

        this.sessionSubscriptions.dispose()
        this.clientSubscriptions.dispose()
        this.subscriptions.dispose()
    }
}