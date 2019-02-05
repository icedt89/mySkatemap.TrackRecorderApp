package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder

import android.app.Activity
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Lifecycle
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.checkedChanges
import com.janhafner.myskatemap.apps.trackrecorder.*
import com.janhafner.myskatemap.apps.trackrecorder.BuildConfig
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.common.*
import com.janhafner.myskatemap.apps.trackrecorder.common.types.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.common.types.TrackingPausedReason
import com.janhafner.myskatemap.apps.trackrecorder.common.types.UserProfile
import com.janhafner.myskatemap.apps.trackrecorder.services.track.ITrackService
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.IServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session.ITrackRecordingSession
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session.TrackRecordingSessionState
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.settings.IUserProfileSettings
import com.janhafner.myskatemap.apps.trackrecorder.views.TabDefinition
import com.janhafner.myskatemap.apps.trackrecorder.views.TabDefinitionTabsAdapter
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.about.AboutActivity
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.appsettings.AppSettingsActivity
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.playground.PlaygroundActivity
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.tracklist.TrackListActivity
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.DashboardTabFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dialogs.DiscardRecordingAlertDialogBuilder
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dialogs.FinishRecordingAlertDialogBuilder
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dialogs.ShowLocationServicesAlertDialogBuilder
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.map.MapTabFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.userprofilesettings.UserProfileSettingsActivity
import com.trello.rxlifecycle3.android.lifecycle.kotlin.bindUntilEvent
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_track_recorder.*
import kotlinx.android.synthetic.main.app_toolbar.*
import kotlinx.android.synthetic.main.navigation_drawer_trackrecorder_activity_info.*



internal final class TrackRecorderActivityPresenter(private val view: TrackRecorderActivity,
                                                    private val trackService: ITrackService,
                                                    private val trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>,
                                                    private val appSettings: IAppSettings,
                                                    private val userProfileSettings: IUserProfileSettings) {
    private val subscriptions: CompositeDisposable = CompositeDisposable()

    private val sessionSubscriptions: CompositeDisposable = CompositeDisposable()

    private val sessionMenuSubscriptions: CompositeDisposable = CompositeDisposable()

    private var trackRecorderSubscription: Disposable? = null

    private var trackRecorderSession: ITrackRecordingSession? = null

    private var finishTrackRecordingMenuItem: MenuItem? = null

    private var discardTrackRecordingMenuItem: MenuItem? = null

    private var identity: Identity = Identity.anonymous()

    private var navigationDrawersOpened = false

    init {
        this.view.setContentView(R.layout.activity_track_recorder)

        this.view.setSupportActionBar(this.view.app_toolbar)

        val actionBar = this.view.supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_bright_24dp)

        val viewPager = this.view.findViewById<ViewPager>(R.id.trackrecorderactivity_toolbar_viewpager)

        val tabDefinitions = listOf(
                TabDefinition(this.view.getString(R.string.trackrecorderactivity_tab_dashboard_title), {
                    DashboardTabFragment()
                }, 0, null),
                TabDefinition(this.view.getString(R.string.trackrecorderactivity_tab_map_title), {
                    //OverviewTabFragment()
                    MapTabFragment()
                }, 1, null)
        )
        viewPager.adapter = TabDefinitionTabsAdapter(tabDefinitions, this.view.supportFragmentManager)
        viewPager.offscreenPageLimit = viewPager.adapter!!.count

        val tabLayout = this.view.findViewById<TabLayout>(R.id.trackrecorderactivity_toolbar_tablayout)
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
        this.setupLeftNavigationView()
        this.setupRightNavigationView()

        this.view.trackrecorderactivity_navigationdrawer.addDrawerListener(object : DrawerLayout.DrawerListener{
            public override fun onDrawerStateChanged(newState: Int) {
            }

            public override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
            }

            public override fun onDrawerClosed(drawerView: View) {
                this@TrackRecorderActivityPresenter.navigationDrawersOpened = false
            }

            public override fun onDrawerOpened(drawerView: View) {
                this@TrackRecorderActivityPresenter.navigationDrawersOpened = true
            }
        })
    }

    private fun getCurrentActivityTextForInfo(trackRecorderSession: ITrackRecordingSession?): String {
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
        this.view.navigation_drawer_trackrecorder_activity_info_live_location.isChecked = this.appSettings.enableLiveLocation
        this.view.navigation_drawer_trackrecorder_activity_info_keep_screen_on.isChecked = this.appSettings.keepScreenOn
        this.view.navigation_drawer_trackrecorder_activity_info_show_my_location.isChecked = this.appSettings.showMyLocation
        this.view.navigation_drawer_trackrecorder_activity_info_show_positions_on_map.isChecked = this.appSettings.showPositionsOnMap

        this.subscriptions.addAll(
                this.view.navigation_drawer_trackrecorder_activity_info_auto_pause_on_still.checkedChanges().subscribe {
                    this.appSettings.enableAutoPauseOnStill = it
                },
                this.view.navigation_drawer_trackrecorder_activity_info_live_location.checkedChanges().subscribe {
                    this.appSettings.enableLiveLocation = it
                },
                this.view.navigation_drawer_trackrecorder_activity_info_keep_screen_on.checkedChanges().subscribe {
                    this.appSettings.keepScreenOn = it
                },
                this.view.navigation_drawer_trackrecorder_activity_info_show_my_location.checkedChanges().subscribe {
                    this.appSettings.showMyLocation = it
                },
                this.view.navigation_drawer_trackrecorder_activity_info_show_positions_on_map.checkedChanges().subscribe {
                    this.appSettings.showPositionsOnMap = it
                },
                this.appSettings.propertyChanged
                        .subscribeOn(Schedulers.computation())
                        .hasChanged()
                        .isNamed(IAppSettings::keepScreenOn.name)
                        .map {
                            it.newValue as Boolean
                        }
                        .subscribe {
                            if (it) {
                                this.view.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                            } else {
                                this.view.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                            }
                        },
                this.appSettings.propertyChanged
                        .subscribeOn(Schedulers.computation())
                        .hasChanged()
                        .observeOn(AndroidSchedulers.mainThread())
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
                                IAppSettings::showMyLocation.name -> {
                                    this.view.navigation_drawer_trackrecorder_activity_info_show_my_location.isChecked = it.newValue as Boolean
                                }
                                IAppSettings::showPositionsOnMap.name -> {
                                    this.view.navigation_drawer_trackrecorder_activity_info_show_positions_on_map.isChecked = it.newValue as Boolean
                                }
                            }
                        }
        )

        if (this.appSettings.keepScreenOn) {
            this.view.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private fun showMenuItems(isVisible: Boolean) {
        if (this.finishTrackRecordingMenuItem != null) {
            this.finishTrackRecordingMenuItem!!.isVisible = isVisible
        }

        if (this.discardTrackRecordingMenuItem != null) {
            this.discardTrackRecordingMenuItem!!.isVisible = isVisible
        }
    }

    private fun createNewTrackRecording(): TrackRecording {
        val result = TrackRecording.start(this.appSettings.defaultMetActivityCode)

        if (this.userProfileSettings.isValidForBurnedEnergyCalculation()) {
            result.userProfile = UserProfile(userProfileSettings.age!!,
                    userProfileSettings.weight!!,
                    userProfileSettings.height!!,
                    userProfileSettings.sex!!)
        }

        return result
    }

    private fun getInitializedSession(trackRecorderSession: ITrackRecordingSession): ITrackRecordingSession {
        this.showMenuItems(true)

        this.sessionSubscriptions.addAll(
                trackRecorderSession.stateChanged
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            var iconId = R.drawable.ic_play_arrow_bright_24dp

                            when (it.state) {
                                TrackRecordingSessionState.Running -> {
                                    iconId = R.drawable.ic_pause_bright_24dp
                                }
                                TrackRecordingSessionState.Paused -> {
                                    if (it.pausedReason == TrackingPausedReason.LocationServicesUnavailable) {
                                        iconId = R.drawable.ic_location_on_bright_24dp
                                    }
                                }
                            }

                            this.view.trackrecorderactivity_main_floatingactionbutton.setImageResource(iconId)

                            if (this.discardTrackRecordingMenuItem != null) {
                                this.discardTrackRecordingMenuItem!!.isEnabled = it.state != TrackRecordingSessionState.Running
                            }

                            if (this.finishTrackRecordingMenuItem != null) {
                                this.finishTrackRecordingMenuItem!!.isEnabled = it.state != TrackRecordingSessionState.Running
                            }
                        },
                trackRecorderSession.stateChanged
                        .subscribeOn(Schedulers.computation())
                        .pairWithPrevious()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { pair ->
                            val previousState = pair.first
                            val currentState = pair.second!!

                            if (currentState.state == TrackRecordingSessionState.Paused
                                    && currentState.pausedReason == TrackingPausedReason.LocationServicesUnavailable) {
                                ToastManager.showToast(this.view, this.view.getString(R.string.trackrecorderactivity_toast_recording_locationservicesunavailable_paused), Toast.LENGTH_SHORT)
                            } else if (previousState != null) {
                                when (currentState.state) {
                                    TrackRecordingSessionState.Running -> {
                                        ToastManager.showToast(this.view, this.view.getString(R.string.trackrecorderactivity_toast_recording_running), Toast.LENGTH_SHORT)
                                    }
                                    TrackRecordingSessionState.Paused -> {
                                        if (currentState.pausedReason == TrackingPausedReason.StillStandDetected) {
                                            ToastManager.showToast(this.view, this.view.getString(R.string.trackrecorderactivity_toast_recording_stillstand_paused), Toast.LENGTH_SHORT)
                                        } else {
                                            ToastManager.showToast(this.view, this.view.getString(R.string.trackrecorderactivity_toast_recording_paused), Toast.LENGTH_SHORT)
                                        }
                                    }
                                }
                            }
                        }
        )

        return trackRecorderSession
    }

    private fun uninitializeSession() {
        this.sessionSubscriptions.clear()

        this.trackRecorderSession = null

        this.showMenuItems(false)
    }

    public fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.view.menuInflater.inflate(R.menu.track_recorder_activity_toolbar_menu, menu)

        if (this.finishTrackRecordingMenuItem == null) {
            this.finishTrackRecordingMenuItem = menu.findItem(R.id.trackrecorderactivity_toolbar_finish_currenttracking)

            this.sessionMenuSubscriptions.add(this.finishTrackRecordingMenuItem!!.clicks()
                    .subscribe {
                        val finishRecordingAlertDialogBuilder = FinishRecordingAlertDialogBuilder(this.view)
                        finishRecordingAlertDialogBuilder.setPositiveButton(R.string.trackrecorderactivity_finish_confirmation_button_yes_label) { _, _ ->
                            this.trackRecorderSession!!.finishTracking()
                        }

                        finishRecordingAlertDialogBuilder.show()
                    })
        }

        if (this.discardTrackRecordingMenuItem == null) {
            this.discardTrackRecordingMenuItem = menu.findItem(R.id.trackrecorderactivity_toolbar_discard_currenttracking)

            this.sessionMenuSubscriptions.add(this.discardTrackRecordingMenuItem!!.clicks()
                    .subscribe {
                        val discardRecordingAlertDialogBuilder = DiscardRecordingAlertDialogBuilder(this.view)
                        discardRecordingAlertDialogBuilder.setPositiveButton(R.string.trackrecorderactivity_discard_confirmation_button_yes_label) { _, _ ->
                            this.trackRecorderSession!!.discardTracking()
                        }

                        discardRecordingAlertDialogBuilder.show()
                    })
        }

        if (this.trackRecorderSession == null && this.trackRecorderServiceController.currentBinder != null) {
            val currentBinder = this.trackRecorderServiceController.currentBinder!!

            if (currentBinder.currentSession != null) {
                this.trackRecorderSession = this.getInitializedSession(currentBinder.currentSession!!)

                // This will return the existing subscription!
                this.trackRecorderSubscription = this.trackRecorderServiceController.startAndBindService()
            }
        }

        this.subscriptions.addAll(
                this.trackRecorderServiceController.isClientBoundChanged
                        .subscribeOn(Schedulers.computation())
                        .flatMap {
                            if (it) {
                                this.trackRecorderServiceController.currentBinder!!.hasCurrentSessionChanged
                            } else {
                                Observable.just(false)
                            }
                        }
                        .bindUntilEvent(this.view, Lifecycle.Event.ON_DESTROY)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            if (it) {
                                // This is the case if the activity is reinitialized
                                // and a session is already ongoing.
                                if (this.trackRecorderSession == null) {
                                    this.trackRecorderSession = this.getInitializedSession(this.trackRecorderServiceController.currentBinder!!.currentSession!!)

                                    this.view.isLocationServicesEnabled()
                                            .onErrorReturn { false }
                                            .filter { it }
                                            .subscribe {
                                                this.trackRecorderSession!!.resumeTracking()
                                            }
                                }
                            } else {
                                if(this.trackRecorderSession != null && this.trackRecorderServiceController.currentBinder!!.currentSession == null) {
                                    this.trackRecorderSubscription?.dispose()
                                    this.trackRecorderSubscription = null
                                }

                                this.uninitializeSession()
                            }

                            this.view.navigation_drawer_trackrecorder_activity_info_activity.text = this.getCurrentActivityTextForInfo(this.trackRecorderSession)
                        }
        )

        return true
    }

    public fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            this.view.trackrecorderactivity_navigationdrawer.openDrawer(GravityCompat.START)
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
        this.subscriptions.dispose()

        this.view.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    public fun onBackPressed(): Boolean {
        val cancelBack = !this.navigationDrawersOpened

        if(this.navigationDrawersOpened) {
            this.view.trackrecorderactivity_navigationdrawer.closeDrawers()

            this.navigationDrawersOpened = false
        }

        return cancelBack
    }

    private fun setupLeftNavigationView() {
        val navigationView = this.view.findViewById<NavigationView>(R.id.trackrecorderactivity_navigation)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            if (menuItem.itemId == R.id.trackrecorderactivity_navigation_drawer_action_user_profile) {
                this.view.startActivity(Intent(this.view, UserProfileSettingsActivity::class.java))
            } else if (menuItem.itemId == R.id.trackrecorderactivity_navigation_drawer_action_settings) {
                this.view.startActivity(Intent(this.view, AppSettingsActivity::class.java))
            } else if (menuItem.itemId == R.id.trackrecorderactivity_navigation_drawer_action_about) {
                this.view.startActivity(Intent(this.view, AboutActivity::class.java))
            } else if (menuItem.itemId == R.id.trackrecorderactivity_navigation_drawer_action_tracklist) {
                this.view.startActivity(Intent(this.view, TrackListActivity::class.java))
            } else if (menuItem.itemId == R.id.trackrecorderactivity_navigation_drawer_action_signin) {
                val googleSignInClient = this.getGoogleSignInClient()

                this.view.startActivityForResult(googleSignInClient.signInIntent, GOOGLE_SIGNIN_REQUEST_CODE)
            } else if (menuItem.itemId == R.id.trackrecorderactivity_navigation_drawer_action_playground) {
                this.view.startActivity(Intent(this.view, PlaygroundActivity::class.java))
            }

            this.view.trackrecorderactivity_navigationdrawer.closeDrawers()

            true
        }
    }

    private fun setupMainFloatingActionButton() {
        this.subscriptions.addAll(
                this.view.locationServicesAvailabilityChanged()
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            var iconId = R.drawable.ic_play_arrow_bright_24dp
                            if (!it) {
                                iconId = R.drawable.ic_location_on_bright_24dp
                            }

                            this.view.trackrecorderactivity_main_floatingactionbutton.setImageResource(iconId)
                        },
                this.view.trackrecorderactivity_main_floatingactionbutton.clicks()
                        .flatMapSingle {
                            this.view.isLocationServicesEnabled()
                                    .onErrorReturn {
                                        if (it is ResolvableApiException && it.statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                                            it.startResolutionForResult(this.view, ENABLE_LOCATION_SERVICES_REQUEST_CODE)
                                        }

                                        false
                                    }
                        }
                        .filter { it }
                        .subscribe {
                            if (this.trackRecorderSession == null) {
                                this.trackRecorderSubscription = this.startNewTrackRecording()
                            } else {
                                val currentState = this.trackRecorderSession!!.currentState

                                when (currentState.state) {
                                    TrackRecordingSessionState.Running ->
                                        this.trackRecorderSession!!.pauseTracking()
                                    TrackRecordingSessionState.Paused ->
                                        this.trackRecorderSession!!.resumeTracking()
                                }
                            }
                        })
    }

    private fun startNewTrackRecording(): Disposable {
        this.trackRecorderServiceController
                .isClientBoundChanged
                .subscribeOn(Schedulers.computation())
                .filter {
                    it
                }
                .first(false)
                .flatMapMaybe {
                    this.trackRecorderServiceController.currentBinder!!
                            .hasCurrentSessionChanged
                            .subscribeOn(Schedulers.computation())
                            .first(false)
                            .toMaybe()
                }
                .filter {
                    !it
                }
                .subscribe {
                    val binder = this.trackRecorderServiceController.currentBinder!!
                    val newTrackRecording = this.createNewTrackRecording()

                    binder.useTrackRecording(newTrackRecording)
                }

        return this.trackRecorderServiceController.startAndBindService()
    }

    public fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == GOOGLE_SIGNIN_REQUEST_CODE) {
            val completedTask = GoogleSignIn.getSignedInAccountFromIntent(data)

            val account = completedTask.getResult(ApiException::class.java)

            val displayName = account!!.displayName!!
            val username = account.email!!
            val accessToken = account.idToken!!

            val googleIdentity = Identity(displayName, username, "Google")
            googleIdentity.accessToken = accessToken

            this.identity = googleIdentity
        } else if(requestCode == ENABLE_LOCATION_SERVICES_REQUEST_CODE)  {
            if(resultCode == Activity.RESULT_OK) {
                this.view.trackrecorderactivity_main_floatingactionbutton.performClick()
            } else if (resultCode ==  Activity.RESULT_CANCELED) {
                ShowLocationServicesAlertDialogBuilder(this.view).create().show()
            }
        }
    }

    private fun getGoogleSignInClient(): GoogleSignInClient {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(BuildConfig.APP_BACKEND_OAUTH2_CLIENTID)
                .build()

        return GoogleSignIn.getClient(this.view, googleSignInOptions)
    }

    companion object {
        public const val GOOGLE_SIGNIN_REQUEST_CODE: Int = 1

        public const val ENABLE_LOCATION_SERVICES_REQUEST_CODE: Int = 2
    }
}