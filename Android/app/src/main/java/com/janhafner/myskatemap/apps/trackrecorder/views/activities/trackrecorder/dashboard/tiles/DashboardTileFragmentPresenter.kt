package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles

import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.IServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session.ITrackRecordingSession
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject

internal abstract class DashboardTileFragmentPresenter(protected val trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>) {
    private val subscriptions: CompositeDisposable = CompositeDisposable()

    private val clientSubscriptions: CompositeDisposable = CompositeDisposable()

    private val sessionSubscriptions: CompositeDisposable = CompositeDisposable()

    private var trackRecorderSession: ITrackRecordingSession? = null

    protected val titleChangedSubject: BehaviorSubject<String> = BehaviorSubject.createDefault("")
    public val titleChanged: Observable<String> = this.titleChangedSubject.subscribeOn(Schedulers.computation())

    protected val valueChangedSubject: BehaviorSubject<String> = BehaviorSubject.createDefault("")
    public val valueChanged: Observable<String> = this.valueChangedSubject.subscribeOn(Schedulers.computation())

    protected val unitChangedSubject: BehaviorSubject<String> = BehaviorSubject.createDefault("")
    public val unitChanged: Observable<String> = this.unitChangedSubject.subscribeOn(Schedulers.computation())

    private fun getInitializedSession(trackRecorderSession: ITrackRecordingSession): ITrackRecordingSession {
        val subscriptions = this.createSubscriptions(trackRecorderSession)
        for (subscription in subscriptions) {
            this.sessionSubscriptions.addAll(subscription)
        }

        return trackRecorderSession
    }

    protected abstract fun createSubscriptions(trackRecorderSession: ITrackRecordingSession): List<Disposable>

    private fun uninitializeSession() {
        this.sessionSubscriptions.clear()

        this.trackRecorderSession = null

        this.resetView()
    }

    protected abstract fun resetView()

    public fun onPause() {
        this.uninitializeSession()

        this.clientSubscriptions.clear()
        this.subscriptions.clear()
    }

    public fun onResume() {
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

    public fun destroy() {
        this.uninitializeSession()

        this.unitChangedSubject.onComplete()
        this.titleChangedSubject.onComplete()
        this.valueChangedSubject.onComplete()

        this.sessionSubscriptions.dispose()
        this.clientSubscriptions.dispose()
        this.subscriptions.dispose()
    }
}