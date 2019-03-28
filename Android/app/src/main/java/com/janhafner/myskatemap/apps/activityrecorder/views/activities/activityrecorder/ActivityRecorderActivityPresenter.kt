package com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.util.Log
import android.view.Menu
import android.view.WindowManager
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.jakewharton.rxbinding2.widget.checkedChanges
import com.janhafner.myskatemap.apps.activityrecorder.*
import com.janhafner.myskatemap.apps.activityrecorder.BuildConfig
import com.janhafner.myskatemap.apps.activityrecorder.R
import com.janhafner.myskatemap.apps.activityrecorder.core.*
import com.janhafner.myskatemap.apps.activityrecorder.core.types.Activity
import com.janhafner.myskatemap.apps.activityrecorder.core.types.TrackingPausedReason
import com.janhafner.myskatemap.apps.activityrecorder.core.types.UserProfile
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.ActivityRecorderServiceBinder
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.IServiceController
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.notifications.ActivityRecorderServiceNotification
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.session.ActivitySessionState
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.session.IActivitySession
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.session.SessionStateInfo
import com.janhafner.myskatemap.apps.activityrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.activityrecorder.settings.IUserProfileSettings
import com.janhafner.myskatemap.apps.activityrecorder.views.ActivityWithAppNavigationPresenter
import com.janhafner.myskatemap.apps.activityrecorder.views.TabDefinition
import com.janhafner.myskatemap.apps.activityrecorder.views.TabDefinitionTabsAdapter
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.dashboard.DashboardTabFragment
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.dialogs.DiscardRecordingAlertDialogBuilder
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.dialogs.FinishRecordingAlertDialogBuilder
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.dialogs.ShowLocationServicesAlertDialogBuilder
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.map.MapTabFragment
import com.uber.autodispose.AutoDispose
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activityrecorder_activity.*
import kotlinx.android.synthetic.main.activityrecorder_activityinfo_navigation.*
import kotlinx.android.synthetic.main.app_navigation.*
import kotlinx.android.synthetic.main.app_toolbar.*


internal final class ActivityRecorderActivityPresenter(view: ActivityRecorderActivity,
                                                       private val activityRecorderServiceController: IServiceController<ActivityRecorderServiceBinder>,
                                                       private val appSettings: IAppSettings,
                                                       private val userProfileSettings: IUserProfileSettings): ActivityWithAppNavigationPresenter<ActivityRecorderActivity>(view, R.layout.activityrecorder_activity) {
    private val sessionSubscriptions: CompositeDisposable = CompositeDisposable()

    private val sessionMenuSubscriptions: CompositeDisposable = CompositeDisposable()

    private var trackRecorderSubscription: Disposable? = null

    private var trackRecorderSession: IActivitySession? = null

    private var identity: Identity = Identity.anonymous()

    init {
        this.view.setSupportActionBar(this.view.app_toolbar)

        this.view.app_navigation_action_activity.setTextColor(this.view.getColor(R.color.appBlue))

        val actionBar = this.view.supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_24dp)

        val viewPager = this.view.trackrecorderactivity_toolbar_viewpager

        val tabDefinitions = listOf(
                TabDefinition(this.view.getString(R.string.activityrecorderactivity_tab_dashboard_title), {
                    DashboardTabFragment()
                }, 0, null),
                TabDefinition(this.view.getString(R.string.activityrecorderactivity_tab_map_title), {
                    //OverviewTabFragment()
                    MapTabFragment()
                }, 1, null)
        )
        viewPager.adapter = TabDefinitionTabsAdapter(tabDefinitions, this.view.supportFragmentManager)
        viewPager.offscreenPageLimit = viewPager.adapter!!.count

        val tabLayout = this.view.trackrecorderactivity_toolbar_tablayout
        tabLayout.setupWithViewPager(viewPager)

        for (tabDefinition in tabDefinitions
                .filter {
                    it.iconResource != null || it.customLayoutResource != null
                }
                .sortedBy {
                    it.position
                }) {
            if (tabDefinition.customLayoutResource != null) {
                tabLayout.getTabAt(tabDefinition.position)!!.setCustomView(tabDefinition.customLayoutResource)

            } else if (tabDefinition.iconResource != null) {
                tabLayout.getTabAt(tabDefinition.position)!!.setIcon(tabDefinition.iconResource)
            }
        }

        this.setupMainFloatingActionButton()
        this.setupRightNavigationView()
    }

    private fun getCurrentActivityTextForInfo(trackRecorderSession: IActivitySession?): String {
        if (trackRecorderSession != null) {
            val activityCode = trackRecorderSession.activityCode
            val activityName = this.view.getActivityName(activityCode)
            if (activityName != null) {
                return activityName
            }

            return this.view.getString(R.string.trackrecorderactivity_navigation_drawer_info_activity_content_unknownactivity, activityCode)
        }

        return this.view.getString(R.string.trackrecorderactivity_navigation_drawer_info_activity_content_nosession)
    }

    private fun setupRightNavigationView() {
        this.view.navigation_drawer_trackrecorder_activity_info_auto_pause_on_still.isChecked = this.appSettings.enableAutoPauseOnStill
        this.view.navigation_drawer_trackrecorder_activity_info_keep_screen_on.isChecked = this.appSettings.keepScreenOn
        this.view.navigation_drawer_trackrecorder_activity_info_live_location.isChecked = this.appSettings.enableLiveLocation
        this.view.navigation_drawer_trackrecorder_activity_info_live_location.isEnabled = BuildConfig.LIVE_LOCATION_ENABLE

        this.view.navigation_drawer_trackrecorder_activity_info_auto_pause_on_still.checkedChanges()
                .subscribe {
                    this.appSettings.enableAutoPauseOnStill = it
                }
        this.view.navigation_drawer_trackrecorder_activity_info_live_location.checkedChanges()
                .subscribe {
                    this.appSettings.enableLiveLocation = it
                }
        this.view.navigation_drawer_trackrecorder_activity_info_keep_screen_on.checkedChanges()
                .subscribe {
                    this.appSettings.keepScreenOn = it
                }

        this.appSettings.propertyChanged
                .subscribeOn(Schedulers.computation())
                .hasChanged()
                .isNamed(IAppSettings::keepScreenOn.name)
                .map {
                    it.newValue as Boolean
                }
                .startWith(this.appSettings.keepScreenOn)
                .`as`(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this.view, Lifecycle.Event.ON_DESTROY)))
                .subscribe {
                    if (it) {
                        this.view.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    } else {
                        this.view.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                }
        this.appSettings.propertyChanged
                .subscribeOn(Schedulers.computation())
                .hasChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .`as`(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this.view, Lifecycle.Event.ON_DESTROY)))
                .subscribe {
                    when (it.propertyName) {
                        IAppSettings::enableAutoPauseOnStill.name -> {
                            this.view.navigation_drawer_trackrecorder_activity_info_auto_pause_on_still.isChecked = it.newValue as Boolean
                        }
                        IAppSettings::enableLiveLocation.name -> {
                            this.view.navigation_drawer_trackrecorder_activity_info_live_location.isChecked = it.newValue as Boolean
                        }
                        IAppSettings::keepScreenOn.name -> {
                            this.view.navigation_drawer_trackrecorder_activity_info_keep_screen_on.isChecked = it.newValue as Boolean
                        }
                    }
                }
    }

    private fun getInitializedSession(trackRecorderSession: IActivitySession): IActivitySession {
        this.sessionSubscriptions.addAll(
                trackRecorderSession.stateChanged
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            this.view.trackrecorderactivity_mainfab.setBehavior(it)
                        }
        )

        return trackRecorderSession
    }

    private fun uninitializeSession() {
        this.sessionSubscriptions.clear()

        this.trackRecorderSession = null
    }

    private fun handleFinishActivity() {
        val finishRecordingAlertDialogBuilder = FinishRecordingAlertDialogBuilder(this.view)
        finishRecordingAlertDialogBuilder.setPositiveButton(R.string.activityrecorderactivity_finish_confirmation_button_yes_label) { _, _ ->
            this.trackRecorderSession!!.finishTracking()
                    .subscribeOn(Schedulers.io())
                    .subscribe()
        }

        finishRecordingAlertDialogBuilder.show()
    }

    private fun handleDiscardActivity() {
        val discardRecordingAlertDialogBuilder = DiscardRecordingAlertDialogBuilder(this.view)
        discardRecordingAlertDialogBuilder.setPositiveButton(R.string.activityrecorderactivity_discard_confirmation_button_yes_label) { _, _ ->
            this.trackRecorderSession!!.discardTracking()
        }

        discardRecordingAlertDialogBuilder.show()
    }

    public fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.view.menuInflater.inflate(R.menu.activityrecorder_completion_menu, menu)

        if (this.trackRecorderSession == null && this.activityRecorderServiceController.currentBinder != null) {
            val currentBinder = this.activityRecorderServiceController.currentBinder!!

            if (currentBinder.currentSession != null) {
                this.trackRecorderSession = this.getInitializedSession(currentBinder.currentSession!!)

                // This will return the existing subscription!
                this.trackRecorderSubscription = this.activityRecorderServiceController.startAndBindService()
            }
        }

        this.activityRecorderServiceController.isClientBoundChanged
                .subscribeOn(Schedulers.computation())
                .flatMap {
                    if (it) {
                        this.activityRecorderServiceController.currentBinder!!.hasCurrentSessionChanged
                    } else {
                        Observable.just(false)
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnDispose {
                    Log.i("TRAP", "DISPOSED this.activityRecorderServiceController.isClientBoundChanged")
                }
                .`as`(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this.view, Lifecycle.Event.ON_DESTROY)))
                .subscribe {
                    if (it) {
                        // This is the case if the activity is reinitialized
                        // and a session is already ongoing.
                        if (this.trackRecorderSession == null) {
                            this.trackRecorderSession = this.getInitializedSession(this.activityRecorderServiceController.currentBinder!!.currentSession!!)

                            this.view.isLocationServicesEnabled()
                                    .onErrorReturn { false }
                                    .filter { it }
                                    .subscribe {
                                        this.trackRecorderSession!!.resumeTracking()
                                    }
                        }
                    } else {
                        if (this.trackRecorderSession != null && this.activityRecorderServiceController.currentBinder!!.currentSession == null) {
                            this.trackRecorderSubscription?.dispose()
                            this.trackRecorderSubscription = null
                        }

                        this.uninitializeSession()
                    }

                    this.view.navigation_drawer_trackrecorder_activity_info_activity.text = this.getCurrentActivityTextForInfo(this.trackRecorderSession)
                }

        return true
    }

    public fun destroy() {
        // IMPORTANT: Do not dispose the trackRecorderSubscription on destroy!
        // Because this would terminate the current session and make a reinitialization
        // after activity creation, with an ongoing session, impossible.
        this.uninitializeSession()

        this.sessionSubscriptions.dispose()
        this.sessionMenuSubscriptions.dispose()

        this.view.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun setupMainFloatingActionButton() {
        this.view.locationServicesAvailabilityChanged()
                .subscribeOn(AndroidSchedulers.mainThread())
                .`as`(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this.view, Lifecycle.Event.ON_DESTROY)))
                .subscribe {
                    if (!it) {
                        this.view.trackrecorderactivity_mainfab.setBehavior(SessionStateInfo(ActivitySessionState.Paused, TrackingPausedReason.LocationServicesUnavailable))
                    } else if(this.trackRecorderSession == null) {
                        this.view.trackrecorderactivity_mainfab.setBehavior(null)
                    }
                }
        this.view.trackrecorderactivity_mainfab.mainClicks
                .flatMapSingle {
                    this.view.isLocationServicesEnabled()
                            .onErrorReturn {
                                if (it is ResolvableApiException && it.statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                                    it.startResolutionForResult(this.view, ENABLE_LOCATION_SERVICES_REQUEST_CODE)
                                }

                                false
                            }
                }
                .filter {
                    it
                }
                .`as`(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this.view, Lifecycle.Event.ON_DESTROY)))
                .subscribe {
                    if (this.trackRecorderSession == null) {
                        this.view.trackrecorderactivity_mainfab.toggleMenu()
                    } else {
                        val currentState = this.trackRecorderSession!!.currentState

                        when (currentState.state) {
                            ActivitySessionState.Running ->
                                this.trackRecorderSession!!.pauseTracking()
                            ActivitySessionState.Paused ->
                                this.trackRecorderSession!!.resumeTracking()
                        }
                    }
                }
        Observable.merge(
                this.view.trackrecorderactivity_mainfab.activityBikeClicks
                    .map {
                        KnownMetDefinitionCodes.BIKING_GENERAL
                    },
                this.view.trackrecorderactivity_mainfab.activityRunningClicks
                    .map {
                        KnownMetDefinitionCodes.RUNNING_JOGGING_GENERAL
                    }
                )
                .`as`(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this.view, Lifecycle.Event.ON_DESTROY)))
                .subscribe {
                    // TODO this.view.startActivityForResult(Intent(WifiManager.ACTION_REQUEST_SCAN_ALWAYS_AVAILABLE), 9)

                    this.trackRecorderSubscription = this.startNewActivity(it)
                }
    }

    private fun startNewActivity(metActivityCode: String): Disposable {
        this.activityRecorderServiceController
                .isClientBoundChanged
                .subscribeOn(Schedulers.computation())
                .filter {
                    it
                }
                .first(false)
                .flatMapMaybe {
                    this.activityRecorderServiceController.currentBinder!!
                            .hasCurrentSessionChanged
                            .subscribeOn(Schedulers.computation())
                            .first(false)
                            .toMaybe()
                }
                .filter {
                    !it
                }
                .subscribe {
                    val binder = this.activityRecorderServiceController.currentBinder!!

                    val newActivity = Activity.start(metActivityCode)

                    if (this.userProfileSettings.isValidForBurnedEnergyCalculation()) {
                        newActivity.userProfile = UserProfile(userProfileSettings.age!!,
                                userProfileSettings.weight!!,
                                userProfileSettings.height!!,
                                userProfileSettings.sex!!)
                    }

                    binder.useActivity(newActivity)
                }

        return this.activityRecorderServiceController.startAndBindService()
    }

    public fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == GOOGLE_SIGNIN_REQUEST_CODE && resultCode == RESULT_OK) {
            try {
                this.identity = GoogleIdentity.fromIntent(data!!)
            } catch (apiException: ApiException) {
                // TODO
                ToastManager.showToast(this.view, "ANMELDUNG NICHT MÖGLICH!", Toast.LENGTH_LONG)
            }

        } else if (requestCode == ENABLE_LOCATION_SERVICES_REQUEST_CODE) {
            if (resultCode == RESULT_CANCELED) {
                ShowLocationServicesAlertDialogBuilder(this.view).create().show()
            }
        }
    }

    public fun handleIntent(intent: Intent) {
        if (intent.action == ActivityRecorderServiceNotification.ACTION_FINISH) {
            this.handleFinishActivity()
        } else if (intent.action == ActivityRecorderServiceNotification.ACTION_DISCARD) {
            this.handleDiscardActivity()
        }
    }
}