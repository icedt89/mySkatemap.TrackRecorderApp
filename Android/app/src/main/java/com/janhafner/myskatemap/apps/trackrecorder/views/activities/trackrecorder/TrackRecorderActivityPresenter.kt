package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.support.design.widget.NavigationView
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewPager
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.checkedChanges
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.common.isLocationServicesEnabled
import com.janhafner.myskatemap.apps.trackrecorder.common.pairWithPrevious
import com.janhafner.myskatemap.apps.trackrecorder.common.startLocationServicesSettingsActivity
import com.janhafner.myskatemap.apps.trackrecorder.common.types.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.common.types.UserProfile
import com.janhafner.myskatemap.apps.trackrecorder.getActivityName
import com.janhafner.myskatemap.apps.trackrecorder.isValidForBurnedEnergyCalculation
import com.janhafner.myskatemap.apps.trackrecorder.locationavailability.ILocationAvailabilityChangedSource
import com.janhafner.myskatemap.apps.trackrecorder.services.track.ITrackService
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.IServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session.ITrackRecordingSession
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session.TrackRecordingSessionState
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session.TrackingPausedReason
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.settings.IUserProfileSettings
import com.janhafner.myskatemap.apps.trackrecorder.views.INeedFragmentVisibilityInfo
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.about.AboutActivity
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.appsettings.AppSettingsActivity
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.tracklist.TrackListActivity
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.DashboardTabFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dialogs.DiscardRecordingAlertDialogBuilder
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dialogs.FinishRecordingAlertDialogBuilder
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dialogs.ShowLocationServicesAlertDialogBuilder
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.map.MapTabFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.userprofilesettings.UserProfileSettingsActivity
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
                                                    private val userProfileSettings: IUserProfileSettings,
                                                    private val locationAvailabilityChangedSource: ILocationAvailabilityChangedSource) : INeedFragmentVisibilityInfo {
    private val subscriptions: CompositeDisposable = CompositeDisposable()

    private val clientSubscriptions: CompositeDisposable = CompositeDisposable()

    private val sessionSubscriptions: CompositeDisposable = CompositeDisposable()

    private val sessionMenuSubscriptions: CompositeDisposable = CompositeDisposable()

    private var trackRecorderSubscription: Disposable? = null

    private var fragment: Fragment? = null

    private var trackRecorderSession: ITrackRecordingSession? = null

    private var finishTrackRecordingMenuItem: MenuItem? = null

    private var discardTrackRecordingMenuItem: MenuItem? = null

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
                }, 0,
                R.drawable.ic_dashboard_bright_24dp),
                TabDefinition(this.view.getString(R.string.trackrecorderactivity_tab_map_title), {
                    //MapTabFragment()
                    MapTabFragment()
                }, 1,
                R.drawable.ic_map_bright_24dp)
        )
        viewPager.adapter = TrackRecorderTabsAdapter(tabDefinitions, this.view.supportFragmentManager)
        viewPager.offscreenPageLimit = viewPager.adapter!!.count

        val tabLayout = this.view.findViewById<TabLayout>(R.id.trackrecorderactivity_toolbar_tablayout)
        tabLayout.setupWithViewPager(viewPager)

        for(tabDefinition in tabDefinitions
                .filter {
                    it.iconResource != null || it.customLayoutResource != null
                }
                .sortedBy {
                    it.position
                }) {
            if(tabDefinition.customLayoutResource != null) {
                tabLayout.getTabAt(tabDefinition.position)!!.setCustomView(tabDefinition.customLayoutResource)

            } else if (tabDefinition.iconResource != null) {
                tabLayout.getTabAt(tabDefinition.position)!!.setIcon(tabDefinition.iconResource)
            }
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                val self = this@TrackRecorderActivityPresenter.view

                if (self.currentFocus == null) {
                    return
                }

                val inputMethodManager = view.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(self.currentFocus?.windowToken, 0)
            }
        })

        this.setupMainFloatingActionButton()
        this.setupLeftNavigationView()
        this.setupRightNavigationView()
    }

    private fun setupRightNavigationView() {
        this.view.navigation_drawer_trackrecorder_activity_info_auto_pause_on_still.isChecked = this.appSettings.enableAutoPauseOnStill
        this.view.navigation_drawer_trackrecorder_activity_info_live_location.isChecked = this.appSettings.enableLiveLocation

        this.subscriptions.addAll(
            this.view.navigation_drawer_trackrecorder_activity_info_auto_pause_on_still.checkedChanges().subscribe{
                this.appSettings.enableAutoPauseOnStill = it
            },
            this.view.navigation_drawer_trackrecorder_activity_info_live_location.checkedChanges().subscribe{
                this.appSettings.enableLiveLocation = it
            },
            this.appSettings.propertyChanged
                    .filter {
                        it.hasChanged
                    }
                    .subscribe{
                        when(it.propertyName) {
                            IAppSettings::enableAutoPauseOnStill.name -> {
                                this.view.navigation_drawer_trackrecorder_activity_info_auto_pause_on_still.isChecked = it.newValue as Boolean
                            }
                            IAppSettings::enableLiveLocation.name -> {
                                this.view.navigation_drawer_trackrecorder_activity_info_live_location.isChecked = it.newValue as Boolean
                            }
                        }
                    }
        )
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
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe{
                            var iconId = R.drawable.ic_play_arrow_bright_24dp

                            when (it.state) {
                                TrackRecordingSessionState.Running -> {
                                    iconId = R.drawable.ic_pause_bright_24dp
                                }
                                TrackRecordingSessionState.Paused -> {
                                    if(it.pausedReason == TrackingPausedReason.LocationServicesUnavailable) {
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
                trackRecorderSession.stateChanged.pairWithPrevious()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { pair ->
                            val previousState = pair.first
                            val currentState = pair.second!!

                            if (currentState.state == TrackRecordingSessionState.Paused
                                    && currentState.pausedReason == TrackingPausedReason.LocationServicesUnavailable) {
                                Toast.makeText(this.view, R.string.trackrecorderactivity_toast_recording_paused, Toast.LENGTH_SHORT).show()

                                this.buildAndShowLocationServicesDialog()
                            } else if(previousState != null) {
                                when (currentState.state) {
                                    TrackRecordingSessionState.Running -> {
                                        Toast.makeText(this.view, R.string.trackrecorderactivity_toast_recording_running, Toast.LENGTH_SHORT).show()
                                    }
                                    TrackRecordingSessionState.Paused -> {
                                        Toast.makeText(this.view, R.string.trackrecorderactivity_toast_recording_paused, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
        )

        val activityName = this.view.getActivityName(trackRecorderSession.activityCode)
        this.view.navigation_drawer_trackrecorder_activity_info_activity.text = activityName

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
                        finishRecordingAlertDialogBuilder.setPositiveButton(R.string.trackrecorderactivity_finish_confirmation_button_yes_label, { _, _ ->
                            val trackRecording = this.trackRecorderSession!!.finishTracking()
                            this.trackService.saveTrackRecording(trackRecording)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe {
                                        _ ->
                                        this.uninitializeSession()

                                        this.clientSubscriptions.clear()
                                        this.trackRecorderSubscription!!.dispose()
                                    }
                        })

                        finishRecordingAlertDialogBuilder.show()
                    })
        }

        if (this.discardTrackRecordingMenuItem == null) {
            this.discardTrackRecordingMenuItem = menu.findItem(R.id.trackrecorderactivity_toolbar_discard_currenttracking)

            this.sessionMenuSubscriptions.add(this.discardTrackRecordingMenuItem!!.clicks()
                    .subscribe {
                        val discardRecordingAlertDialogBuilder = DiscardRecordingAlertDialogBuilder(this.view)
                        discardRecordingAlertDialogBuilder.setPositiveButton(R.string.trackrecorderactivity_discard_confirmation_button_yes_label, { _, _ ->
                            this.trackRecorderSession!!.discardTracking()

                            this.uninitializeSession()

                            this.clientSubscriptions.clear()
                            this.trackRecorderSubscription!!.dispose()
                        })

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
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            if (it) {
                                this.clientSubscriptions.addAll(
                                        this.trackRecorderServiceController.currentBinder!!.hasCurrentSessionChanged
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe {
                                                    if (it) {
                                                        // This is the case if the activity is reinitialized
                                                        // and a session is already ongoing.
                                                        if(this.trackRecorderSession == null) {
                                                            this.trackRecorderSession = this.getInitializedSession(this.trackRecorderServiceController.currentBinder!!.currentSession!!)

                                                            if (this.view.isLocationServicesEnabled()) {
                                                                this.trackRecorderSession!!.resumeTracking()
                                                            }
                                                        }
                                                    } else {
                                                        this.uninitializeSession()
                                                    }
                                                }
                                )
                            } else {
                                this.uninitializeSession()

                                this.clientSubscriptions.clear()
                            }
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
        this.uninitializeSession()

        this.sessionSubscriptions.dispose()
        this.sessionMenuSubscriptions.dispose()
        this.clientSubscriptions.dispose()
        this.subscriptions.dispose()

        // IMPORTANT: Do not dispose the trackRecorderSubscription on destroy!
        // Because this would terminate the current session and make a reinitialization
        // after activity creation, with an ongoing session, impossible.
    }

    private fun buildAndShowLocationServicesDialog() {
        ShowLocationServicesAlertDialogBuilder(this.view)
                .setPositiveButton(R.string.trackrecorderactivity_show_location_services_confirmation_open_label, object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        this@TrackRecorderActivityPresenter.view.startLocationServicesSettingsActivity()
                    }
                }).create().show()
    }

    private fun setupLeftNavigationView() {
        val navigationView = this.view.findViewById<NavigationView>(R.id.trackrecorderactivity_navigation)
        navigationView.setNavigationItemSelectedListener(
                object : NavigationView.OnNavigationItemSelectedListener {
                    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
                        if (menuItem.itemId == R.id.trackrecorderactivity_navigation_drawer_action_user_profile) {
                            this@TrackRecorderActivityPresenter.view.startActivity(Intent(this@TrackRecorderActivityPresenter.view, UserProfileSettingsActivity::class.java))
                        } else if (menuItem.itemId == R.id.trackrecorderactivity_navigation_drawer_action_settings) {
                            this@TrackRecorderActivityPresenter.view.startActivity(Intent(this@TrackRecorderActivityPresenter.view, AppSettingsActivity::class.java))
                        } else if(menuItem.itemId == R.id.trackrecorderactivity_navigation_drawer_action_about) {
                            this@TrackRecorderActivityPresenter.view.startActivity(Intent(this@TrackRecorderActivityPresenter.view, AboutActivity::class.java))
                        } else if(menuItem.itemId == R.id.trackrecorderactivity_navigation_drawer_action_tracklist) {
                            this@TrackRecorderActivityPresenter.view.startActivity(Intent(this@TrackRecorderActivityPresenter.view, TrackListActivity::class.java))
                        } else if(menuItem.itemId == R.id.trackrecorderactivity_navigation_drawer_action_signin) {
                            throw NotImplementedError()
                        }

                        this@TrackRecorderActivityPresenter.view.trackrecorderactivity_navigationdrawer.closeDrawers()

                        return true
                    }
                })
    }

    private fun setupMainFloatingActionButton() {
        this.subscriptions.addAll(
                this.locationAvailabilityChangedSource.locationAvailable
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe{
                    var iconId = R.drawable.ic_play_arrow_bright_24dp
                    if (!it) {
                        iconId = R.drawable.ic_location_on_bright_24dp
                    }

                    this.view.trackrecorderactivity_main_floatingactionbutton.setImageResource(iconId)
                },
                this.view.trackrecorderactivity_main_floatingactionbutton.clicks()
                .subscribe {
                    if(!this.view.isLocationServicesEnabled()) {
                        this.buildAndShowLocationServicesDialog()
                    } else {
                        if (this.trackRecorderSession == null) {
                            this.trackRecorderSubscription = this.startNewTrackRecording()
                        } else {
                            val currentState = this.trackRecorderSession!!.currentState

                            when (currentState.state) {
                                TrackRecordingSessionState.Running ->
                                    this.trackRecorderSession!!.pauseTracking()
                                TrackRecordingSessionState.Paused ->
                                    if(currentState.pausedReason == TrackingPausedReason.LocationServicesUnavailable) {
                                        this.buildAndShowLocationServicesDialog()
                                    } else {
                                        this.trackRecorderSession!!.resumeTracking()
                                    }
                            }
                        }
                    }
                })
    }

    private fun startNewTrackRecording(): Disposable {
        this.trackRecorderServiceController
                .isClientBoundChanged
                .filter {
                    it
                }
                .first(false)
                .subscribe { _ ->
                    this.trackRecorderServiceController.currentBinder!!
                            .hasCurrentSessionChanged
                            .filter {
                                !it
                            }
                            .first(false)
                            .subscribe { _ ->
                                val binder = this.trackRecorderServiceController.currentBinder!!
                                val newTrackRecording = this.createNewTrackRecording()

                                binder.useTrackRecording(newTrackRecording)
                            }
                }

        return this.trackRecorderServiceController.startAndBindService()
    }

    public override fun onFragmentVisibilityChange(fragment: Fragment, isVisibleToUser: Boolean) {
    }
}