package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles

import androidx.lifecycle.Lifecycle
import com.janhafner.myskatemap.apps.trackrecorder.core.types.DashboardTileDisplayType
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.IServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session.ITrackRecordingSession
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.IDashboardTileFragmentPresenterConnector
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.LineChartDashboardTileFragmentPresenterConnector
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.TextOnlyDashboardTileFragmentPresenterConnector
import com.uber.autodispose.AutoDispose
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_dashboard_tile_default.*

internal abstract class DashboardTileFragmentPresenter(
        protected val trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>) {
    private val subscriptions: CompositeDisposable = CompositeDisposable()

    private val sessionSubscriptions: CompositeDisposable = CompositeDisposable()

    private var trackRecorderSession: ITrackRecordingSession? = null

    protected var presenterConnector: IDashboardTileFragmentPresenterConnector = TextOnlyDashboardTileFragmentPresenterConnector()
        private set

    private var dashboardTileFragment: DashboardTileFragment? = null

    public open val supportedPresenterConnectorTypes: List<DashboardTileDisplayType> = listOf(DashboardTileDisplayType.TextOnly)

    public var displayType: DashboardTileDisplayType = DashboardTileDisplayType.TextOnly
        set(value) {
            if (!this.supportedPresenterConnectorTypes.contains(value)) {
                throw IllegalArgumentException("Unsupported presenter connector type: ${value}")
            }

            if (value == field) {
                return
            }

            if (value == DashboardTileDisplayType.TextOnly) {
                this.presenterConnector = TextOnlyDashboardTileFragmentPresenterConnector()
            } else if(value == DashboardTileDisplayType.LineChart) {
                this.presenterConnector = LineChartDashboardTileFragmentPresenterConnector()
            } else {
                throw java.lang.IllegalArgumentException("Unknown tile display type: ${value}")
            }

            field = value

            this.initialize(this.dashboardTileFragment!!)
        }

    public var title: String = ""
        protected set

    protected fun subscribe(dashboardTileFragment: DashboardTileFragment, source: Observable<FormattedDisplayValue>) {
        val subscriptions = this.presenterConnector.connect(dashboardTileFragment, source)

        this.sessionSubscriptions.clear()

        for (subscription in subscriptions) {
            this.sessionSubscriptions.add(subscription)
        }
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

    public fun initialize(dashboardTileFragment: DashboardTileFragment) {
        this.subscriptions.clear()

        this.dashboardTileFragment = dashboardTileFragment

        dashboardTileFragment.fragment_dashboard_tile_title.text = this.title

        this.subscriptions.add(
                this.trackRecorderServiceController.isClientBoundChanged
                        .flatMap {
                            if (it) {
                                this.trackRecorderServiceController.currentBinder!!.hasCurrentSessionChanged
                            } else {
                                Observable.just(false)
                            }
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .`as`(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this.dashboardTileFragment, Lifecycle.Event.ON_DESTROY)))
                        .subscribe {
                            if (it) {
                                if (this.dashboardTileFragment != null) {
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
        this.sessionSubscriptions.dispose()
        this.subscriptions.dispose()

        this.dashboardTileFragment = null
    }
}