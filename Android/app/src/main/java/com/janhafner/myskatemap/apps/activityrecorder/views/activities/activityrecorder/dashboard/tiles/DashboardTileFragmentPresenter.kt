package com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.dashboard.tiles

import androidx.lifecycle.Lifecycle
import com.janhafner.myskatemap.apps.activityrecorder.core.types.DashboardTileDisplayType
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.IServiceController
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.ActivityRecorderServiceBinder
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.session.IActivitySession
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.dashboard.IDashboardTileFragmentPresenterConnector
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.dashboard.LineChartDashboardTileFragmentPresenterConnector
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.dashboard.TextOnlyDashboardTileFragmentPresenterConnector
import com.uber.autodispose.AutoDispose
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activityrecorder_dashboard_tile_default_fragment.*

internal abstract class DashboardTileFragmentPresenter(
        protected val activityRecorderServiceController: IServiceController<ActivityRecorderServiceBinder>) {
    private val subscriptions: CompositeDisposable = CompositeDisposable()

    private val sessionSubscriptions: CompositeDisposable = CompositeDisposable()

    private var trackRecorderSession: IActivitySession? = null

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

    protected abstract fun getSessionBoundObservable(trackRecorderSession: IActivitySession): Observable<FormattedDisplayValue>

    private fun getInitializedSession(trackRecorderSession: IActivitySession): IActivitySession {
        val observable = this.getSessionBoundObservable(trackRecorderSession)

        this.subscribe(this.dashboardTileFragment!!, observable)

        return trackRecorderSession
    }

    public fun initialize(dashboardTileFragment: DashboardTileFragment) {
        this.subscriptions.clear()

        this.dashboardTileFragment = dashboardTileFragment

        dashboardTileFragment.fragment_dashboard_tile_title.text = this.title

        this.subscriptions.add(
                this.activityRecorderServiceController.isClientBoundChanged
                        .flatMap {
                            if (it) {
                                this.activityRecorderServiceController.currentBinder!!.hasCurrentSessionChanged
                            } else {
                                Observable.just(false)
                            }
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .`as`(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this.dashboardTileFragment, Lifecycle.Event.ON_DESTROY)))
                        .subscribe {
                            if (it) {
                                if (this.dashboardTileFragment != null) {
                                    this.trackRecorderSession = this.getInitializedSession(this.activityRecorderServiceController.currentBinder!!.currentSession!!)
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