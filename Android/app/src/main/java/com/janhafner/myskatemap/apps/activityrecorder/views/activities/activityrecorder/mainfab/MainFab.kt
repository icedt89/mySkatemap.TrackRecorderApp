package com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.mainfab

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.jakewharton.rxbinding2.view.clicks
import com.janhafner.myskatemap.apps.activityrecorder.R
import com.janhafner.myskatemap.apps.activityrecorder.core.types.TrackingPausedReason
import com.janhafner.myskatemap.apps.activityrecorder.menuClicks
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.session.ActivitySessionState
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.session.SessionStateInfo
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activityrecorder_mainfab.view.*

internal interface IMainFabBehavior {
    val mainClicks: Observable<Unit>

    val activityBikeClicks: Observable<Unit>

    val activityRunningClicks: Observable<Unit>

    val finishClicks: Observable<Unit>

    val discardClicks: Observable<Unit>

    fun apply()
}

internal final class DefaultNonSessionBoundMainFabBehavior(private val mainFab: MainFab) : IMainFabBehavior {
    public override val mainClicks: Observable<Unit>
        get() = this.mainFab.main_fab.menuClicks()
                .map{
                    Unit
                }

    public override val activityBikeClicks: Observable<Unit>
        get() = this.mainFab.main_fab_menu_item_bike.clicks()
                .map{
                    Unit
                }

    public override val activityRunningClicks: Observable<Unit>
        get() = this.mainFab.main_fab_menu_item_running.clicks()
                .map{
                    Unit
                }

    public override val finishClicks: Observable<Unit>
        get() = Observable.never()

    public override val discardClicks: Observable<Unit>
        get() = Observable.never()

    public override fun apply() {
        this.mainFab.main_fab.visibility = CoordinatorLayout.VISIBLE
        this.mainFab.main_fab_menu_item_bike.visibility = CoordinatorLayout.INVISIBLE
        this.mainFab.main_fab_menu_item_running.visibility = CoordinatorLayout.INVISIBLE
        this.mainFab.main_fab_menu_item_finish.visibility = CoordinatorLayout.GONE
        this.mainFab.main_fab_menu_item_discard.visibility = CoordinatorLayout.GONE
        this.mainFab.main_fab_running.visibility = CoordinatorLayout.GONE
        this.mainFab.main_fab_nolocationservices.visibility = CoordinatorLayout.GONE

        this.mainFab.main_fab.menuIconView.setImageResource(R.drawable.ic_add_24dp)
    }
}

internal final class SessionBoundAndRunningMainFabBehavior(private val mainFab: MainFab) : IMainFabBehavior {
    public override val mainClicks: Observable<Unit>
        get() = this.mainFab.main_fab_running.clicks()
                .map{
                    Unit
                }

    public override val activityBikeClicks: Observable<Unit>
        get() = Observable.never()

    public override val activityRunningClicks: Observable<Unit>
        get() = Observable.never()

    public override val finishClicks: Observable<Unit>
        get() = Observable.never()

    public override val discardClicks: Observable<Unit>
        get() = Observable.never()

    public override fun apply() {
        this.mainFab.main_fab.visibility = CoordinatorLayout.GONE
        this.mainFab.main_fab_menu_item_bike.visibility = CoordinatorLayout.GONE
        this.mainFab.main_fab_menu_item_running.visibility = CoordinatorLayout.GONE
        this.mainFab.main_fab_menu_item_finish.visibility = CoordinatorLayout.GONE
        this.mainFab.main_fab_menu_item_discard.visibility = CoordinatorLayout.GONE
        this.mainFab.main_fab_nolocationservices.visibility = CoordinatorLayout.GONE
        this.mainFab.main_fab_running.visibility = CoordinatorLayout.VISIBLE
    }
}

internal final class SessionBoundAndPausedMainFabBehavior(private val mainFab: MainFab) : IMainFabBehavior {
    public override val mainClicks: Observable<Unit>
        get() = this.mainFab.main_fab.menuClicks()
                .map{
                    Unit
                }

    public override val activityBikeClicks: Observable<Unit>
        get() = Observable.never()

    public override val activityRunningClicks: Observable<Unit>
        get() = Observable.never()

    public override val finishClicks: Observable<Unit>
        get() = this.mainFab.main_fab_menu_item_finish.clicks()
                .map{
                    Unit
                }

    public override val discardClicks: Observable<Unit>
        get() = this.mainFab.main_fab_menu_item_discard.clicks()
                .map{
                    Unit
                }

    public override fun apply() {
        this.mainFab.main_fab.visibility = CoordinatorLayout.VISIBLE
        this.mainFab.main_fab_menu_item_finish.visibility = CoordinatorLayout.INVISIBLE
        this.mainFab.main_fab_menu_item_discard.visibility = CoordinatorLayout.INVISIBLE
        this.mainFab.main_fab_menu_item_bike.visibility = CoordinatorLayout.GONE
        this.mainFab.main_fab_menu_item_running.visibility = CoordinatorLayout.GONE
        this.mainFab.main_fab_nolocationservices.visibility = CoordinatorLayout.GONE
        this.mainFab.main_fab_running.visibility = CoordinatorLayout.GONE

        this.mainFab.main_fab.menuIconView.setImageResource(R.drawable.ic_play_arrow_24dp)
    }
}

internal final class NoLocationServicesMainFabBehavior(private val mainFab: MainFab) : IMainFabBehavior {
    public override val mainClicks: Observable<Unit>
        get() = this.mainFab.main_fab_nolocationservices.clicks()
                .map{
                    Unit
                }

    public override val activityBikeClicks: Observable<Unit>
        get() = Observable.never()

    public override val activityRunningClicks: Observable<Unit>
        get() = Observable.never()

    public override val finishClicks: Observable<Unit>
        get() = Observable.never()

    public override val discardClicks: Observable<Unit>
        get() = Observable.never()

    public override fun apply() {
        this.mainFab.main_fab.visibility = CoordinatorLayout.GONE
        this.mainFab.main_fab_menu_item_bike.visibility = CoordinatorLayout.GONE
        this.mainFab.main_fab_menu_item_running.visibility = CoordinatorLayout.GONE
        this.mainFab.main_fab_menu_item_finish.visibility = CoordinatorLayout.GONE
        this.mainFab.main_fab_menu_item_discard.visibility = CoordinatorLayout.GONE
        this.mainFab.main_fab_nolocationservices.visibility = CoordinatorLayout.VISIBLE
        this.mainFab.main_fab_running.visibility = CoordinatorLayout.GONE
    }
}

internal final class MainFab(context: Context, attributeSet: AttributeSet) : CoordinatorLayout(context, attributeSet) {
    private val defaultNonSessionBoundMainFabBehavior: IMainFabBehavior = DefaultNonSessionBoundMainFabBehavior(this)
    private val sessionBoundAndRunningMainFabBehavior: IMainFabBehavior = SessionBoundAndRunningMainFabBehavior(this)
    private val sessionBoundAndPausedMainFabBehavior: IMainFabBehavior = SessionBoundAndPausedMainFabBehavior(this)
    private val noLocationServicesMainFabBehavior: IMainFabBehavior = NoLocationServicesMainFabBehavior(this)

    private var currentMainFabBehavior: IMainFabBehavior? = null

    init {
        val inflater = context.getSystemService(LayoutInflater::class.java)

        inflater.inflate(R.layout.activityrecorder_mainfab, this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        this.setBehavior(null)
    }

    public val mainClicks: Observable<Unit>
        get() = this.defaultNonSessionBoundMainFabBehavior.mainClicks
                .mergeWith(this.noLocationServicesMainFabBehavior.mainClicks)
                .mergeWith(this.sessionBoundAndPausedMainFabBehavior.mainClicks)
                .mergeWith(this.sessionBoundAndRunningMainFabBehavior.mainClicks)

    public val activityBikeClicks: Observable<Unit>
        get() = this.defaultNonSessionBoundMainFabBehavior.activityBikeClicks
                .mergeWith(this.noLocationServicesMainFabBehavior.activityBikeClicks)
                .mergeWith(this.sessionBoundAndPausedMainFabBehavior.activityBikeClicks)
                .mergeWith(this.sessionBoundAndRunningMainFabBehavior.activityBikeClicks)

    public val activityRunningClicks: Observable<Unit>
        get() = this.defaultNonSessionBoundMainFabBehavior.activityRunningClicks
                .mergeWith(this.noLocationServicesMainFabBehavior.activityRunningClicks)
                .mergeWith(this.sessionBoundAndPausedMainFabBehavior.activityRunningClicks)
                .mergeWith(this.sessionBoundAndRunningMainFabBehavior.activityRunningClicks)

    public val finishClicks: Observable<Unit>
        get() = this.defaultNonSessionBoundMainFabBehavior.finishClicks
                .mergeWith(this.noLocationServicesMainFabBehavior.finishClicks)
                .mergeWith(this.sessionBoundAndPausedMainFabBehavior.finishClicks)
                .mergeWith(this.sessionBoundAndRunningMainFabBehavior.finishClicks)

    public val discardClicks: Observable<Unit>
        get() = this.defaultNonSessionBoundMainFabBehavior.discardClicks
                .mergeWith(this.noLocationServicesMainFabBehavior.discardClicks)
                .mergeWith(this.sessionBoundAndPausedMainFabBehavior.discardClicks)
                .mergeWith(this.sessionBoundAndRunningMainFabBehavior.discardClicks)

    public fun setBehavior(sessionStateInfo: SessionStateInfo?) {
        if(sessionStateInfo == null) {
            // NO SESSION
            this.setBehaviorAndApply(this.defaultNonSessionBoundMainFabBehavior)
        } else {
            // SESSION AVAILABLE
            if(sessionStateInfo.state == ActivitySessionState.Running) {
                // SESSION IS RUNNING
                this.setBehaviorAndApply(this.sessionBoundAndRunningMainFabBehavior)
            } else  {
                // SESSION IS PAUSED
                if(sessionStateInfo.pausedReason == TrackingPausedReason.LocationServicesUnavailable) {
                    // PAUSED BECAUSE NO LOCATION
                    this.setBehaviorAndApply(this.noLocationServicesMainFabBehavior)
                } else {
                    // ANY OTHER REASON
                    this.setBehaviorAndApply(this.sessionBoundAndPausedMainFabBehavior)
                }
            }
        }
    }

    private fun setBehaviorAndApply(mainFabBehavior: IMainFabBehavior) {
        if (this.currentMainFabBehavior == mainFabBehavior) {
            return
        }

        this.main_fab.close(false)

        this.currentMainFabBehavior = mainFabBehavior

        mainFabBehavior.apply()
    }

    public fun toggleMenu() {
        this.main_fab.toggle(true)
    }

    public fun closeMenu() {
        this.main_fab.close(true)
    }
}

