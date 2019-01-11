package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles

import com.jakewharton.rxbinding2.widget.text
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.IServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session.ITrackRecordingSession
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_dashboard_tile_default.*

internal abstract class DashboardTileFragmentPresenter(
        protected val trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>) {
    private val subscriptions: CompositeDisposable = CompositeDisposable()

    private val sessionSubscriptions: CompositeDisposable = CompositeDisposable()

    private var trackRecorderSession: ITrackRecordingSession? = null

    protected var dashboardTileFragment: DashboardTileFragment? = null

    public var title: String = ""
        protected set

    public var formattedValueChanged: Observable<FormattedDisplayValue> = Observable.empty()
        protected set

    private fun subscribe(dashboardTileFragment: DashboardTileFragment, source: Observable<FormattedDisplayValue>) {
        this.sessionSubscriptions.addAll(
                source.map {
                    it.value
                }
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(dashboardTileFragment.fragment_dashboard_tile_value.text()),
                source.map {
                    it.unit
                }
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(dashboardTileFragment.fragment_dashboard_tile_unit.text())
        )
    }

    private fun uninitializeSession() {
        this.sessionSubscriptions.clear()

        this.trackRecorderSession = null
    }

    protected abstract fun getResetObservable(): Observable<FormattedDisplayValue>

    private fun reset() {
        val observable = this.getResetObservable()

        this.subscribe(this.dashboardTileFragment!!, observable)
    }

    protected abstract fun getSessionBoundObservable(trackRecorderSession: ITrackRecordingSession): Observable<FormattedDisplayValue>

    private fun getInitializedSession(trackRecorderSession: ITrackRecordingSession): ITrackRecordingSession {
        val observable = this.getSessionBoundObservable(trackRecorderSession)

        this.subscribe(this.dashboardTileFragment!!, observable)

        return trackRecorderSession
    }

    public fun onPause() {
        this.uninitializeSession()

        this.subscriptions.clear()
    }

    public fun onResume(dashboardTileFragment: DashboardTileFragment) {
        this.subscriptions.clear()

        this.dashboardTileFragment = dashboardTileFragment

        dashboardTileFragment.fragment_dashboard_tile_title.text = this.title

        this.subscriptions.addAll(
                this.trackRecorderServiceController.isClientBoundChanged
                        .flatMap {
                            if(it) {
                                this.trackRecorderServiceController.currentBinder!!.hasCurrentSessionChanged
                            } else {
                                Observable.just(false)
                            }
                        }
                        .subscribe{
                            if(it){
                                if(this.dashboardTileFragment != null) {
                                    this.trackRecorderSession = this.getInitializedSession(this.trackRecorderServiceController.currentBinder!!.currentSession!!)
                                }
                            } else {
                                this.uninitializeSession()

                                this.reset()
                            }
                        }
                    )
    }

    public fun destroy() {
        this.uninitializeSession()
        this.reset()

        this.sessionSubscriptions.dispose()
        this.subscriptions.dispose()
    }
}