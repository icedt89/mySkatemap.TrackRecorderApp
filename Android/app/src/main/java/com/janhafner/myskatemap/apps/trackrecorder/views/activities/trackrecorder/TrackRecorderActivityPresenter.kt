package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder

import android.app.Activity
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
import com.janhafner.myskatemap.apps.trackrecorder.*
import com.janhafner.myskatemap.apps.trackrecorder.services.ITrackService
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.IServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.ITrackRecordingSession
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecordingSessionState
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.data.UserProfile
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.data.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.settings.IUserProfile
import com.janhafner.myskatemap.apps.trackrecorder.views.INeedFragmentVisibilityInfo
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.appsettings.AppSettingsActivity
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dialogs.DiscardRecordingAlertDialogBuilder
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dialogs.FinishRecordingAlertDialogBuilder
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dialogs.ShowLocationServicesAlertDialogBuilder
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.userprofilesettings.UserProfileSettingsActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_track_recorder.*
import kotlinx.android.synthetic.main.app_toolbar.*
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.lang.IllegalArgumentException


internal final class TrackRecorderActivityPresenter(private val view: TrackRecorderActivity,
                                                    private val trackService: ITrackService,
                                                    private val trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>,
                                                    private val appSettings: IAppSettings,
                                                    private val userProfile: IUserProfile) : INeedFragmentVisibilityInfo {
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
        viewPager.adapter = TrackRecorderTabsAdapter(this.view, this.view.supportFragmentManager)
        viewPager.offscreenPageLimit = viewPager.adapter!!.count

        val tabLayout = this.view.findViewById<TabLayout>(R.id.trackrecorderactivity_toolbar_tablayout)
        tabLayout.setupWithViewPager(viewPager)
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

        // TODO:
        val navigationView = this.view.findViewById<NavigationView>(R.id.trackrecorderactivity_navigation)
        navigationView.setNavigationItemSelectedListener(
                object : NavigationView.OnNavigationItemSelectedListener {
                    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
                        if (menuItem.itemId == R.id.trackrecorderactivity_navigation_drawer_action_user_profile) {
                            this@TrackRecorderActivityPresenter.view.startActivity(Intent(this@TrackRecorderActivityPresenter.view, UserProfileSettingsActivity::class.java))
                        } else if (menuItem.itemId == R.id.trackrecorderactivity_navigation_drawer_action_settings) {
                            this@TrackRecorderActivityPresenter.view.startActivity(Intent(this@TrackRecorderActivityPresenter.view, AppSettingsActivity::class.java))
                        }

                        this@TrackRecorderActivityPresenter.view.trackrecorderactivity_navigationdrawer.closeDrawers()

                        return true
                    }
                })

        if (this.trackRecorderSession == null && this.trackRecorderServiceController.currentBinder != null) {
            val currentBinder = this.trackRecorderServiceController.currentBinder!!

            if (currentBinder.currentSession != null) {
                this.trackRecorderSession = this.getInitializedSession(currentBinder.currentSession!!)
            }
        }

        this.setupMainFloatingActionButton()
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
        val dateTimeFormatter: DateTimeFormatter = DateTimeFormat.shortDateTime()
        val nameTemplate = this.view.getString(R.string.trackrecorderactivity_presenter_default_new_trackrecording_name_template)

        val trackRecordingName: String = String.format(nameTemplate, dateTimeFormatter.print(DateTime.now()))

        val result = TrackRecording.start(trackRecordingName, this.appSettings.locationProviderTypeName)
        if (this.userProfile.isValidForBurnedEnergyCalculation()) {
            result.userProfile = UserProfile(userProfile.age!!,
                    this.appSettings.defaultMetActivityCode,
                    userProfile.weight!!,
                    userProfile.height!!,
                    userProfile.sex!!)
        }

        return result
    }

    private fun getInitializedSession(trackRecorderSession: ITrackRecordingSession): ITrackRecordingSession {
        this.showMenuItems(true)

        this.sessionSubscriptions.addAll(
                trackRecorderSession.stateChanged.pairWithPrevious()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { pair ->
                            var iconId = R.drawable.ic_pause_bright_24dp

                            val previousState = pair.first
                            val currentState = pair.second

                            if(currentState == TrackRecordingSessionState.LocationServicesUnavailable) {
                                Toast.makeText(this.view, R.string.trackrecorderactivity_toast_recording_paused, Toast.LENGTH_LONG).show()

                                iconId = R.drawable.ic_location_on_bright_24dp

                                this.buildAndShowLocationServicesDialog()
                            } else if(previousState != null) {
                                when (currentState) {
                                    TrackRecordingSessionState.Running -> {
                                        Toast.makeText(this.view, R.string.trackrecorderactivity_toast_recording_running, Toast.LENGTH_LONG).show()
                                    }
                                    TrackRecordingSessionState.Paused -> {
                                        Toast.makeText(this.view, R.string.trackrecorderactivity_toast_recording_paused, Toast.LENGTH_LONG).show()

                                        iconId = R.drawable.ic_play_arrow_bright_24dp
                                    }
                                    else -> {
                                        // Nothing happens here, else branch exist only to prevent warning on compile Oo
                                    }
                                }
                            }

                            this.view.trackrecorderactivity_main_floatingactionbutton.setImageResource(iconId)

                            if (this.discardTrackRecordingMenuItem != null) {
                                this.discardTrackRecordingMenuItem!!.isEnabled = currentState != TrackRecordingSessionState.Running
                            }

                            if (this.finishTrackRecordingMenuItem != null) {
                                this.finishTrackRecordingMenuItem!!.isEnabled = currentState != TrackRecordingSessionState.Running
                            }
                        }
        )

        return trackRecorderSession
    }

    private fun uninitializeSession() {
        this.sessionSubscriptions.clear()
        this.sessionMenuSubscriptions.clear()

        this.trackRecorderSession = null

        this.showMenuItems(false)
    }

    public fun onMenuOpened(): Boolean {
        if (this.sessionMenuSubscriptions.size() == 0) {
            if (this.finishTrackRecordingMenuItem != null) {
                this.sessionMenuSubscriptions.add(this.finishTrackRecordingMenuItem!!.clicks()
                        .subscribe {
                            val finishRecordingAlertDialogBuilder = FinishRecordingAlertDialogBuilder(this.view)
                            finishRecordingAlertDialogBuilder.setPositiveButton(R.string.trackrecorderactivity_finish_confirmation_button_yes_label, { _, _ ->
                                // TODO: Make something useful with it (history, upload etc...)
                                // TODO: Make non-blocking because of io
                                val trackRecording = this.trackRecorderSession!!.finishTracking()

                                this.uninitializeSession()

                                this.clientSubscriptions.clear()
                                this.trackRecorderSubscription!!.dispose()
                            })

                            finishRecordingAlertDialogBuilder.show()
                        })
            }

            if (this.discardTrackRecordingMenuItem != null) {
                this.sessionMenuSubscriptions.add(this.discardTrackRecordingMenuItem!!.clicks()
                        .subscribe {
                            val discardRecordingAlertDialogBuilder = DiscardRecordingAlertDialogBuilder(this.view)
                            discardRecordingAlertDialogBuilder.setPositiveButton(R.string.trackrecorderactivity_discard_confirmation_button_yes_label, { _, _ ->
                                // TODO: Make non-blocking because of io
                                this.trackRecorderSession!!.discardTracking()

                                this.uninitializeSession()

                                this.clientSubscriptions.clear()
                                this.trackRecorderSubscription!!.dispose()
                            })

                            discardRecordingAlertDialogBuilder.show()
                        })
            }
        }

        return true
    }

    public fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.view.menuInflater.inflate(R.menu.track_recorder_activity_toolbar_menu, menu)

        if (this.finishTrackRecordingMenuItem == null) {
            this.finishTrackRecordingMenuItem = menu.findItem(R.id.trackrecorderactivity_toolbar_finish_currenttracking)
        }

        if (this.discardTrackRecordingMenuItem == null) {
            this.discardTrackRecordingMenuItem = menu.findItem(R.id.trackrecorderactivity_toolbar_discard_currenttracking)
        }

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
        // Because this would terminate the current session and make an reinitialization
        // after activity creation, with an ongoing session impossible.
    }

    private fun buildAndShowLocationServicesDialog() {
        ShowLocationServicesAlertDialogBuilder(this.view)
                .setPositiveButton(R.string.trackrecorderactivity_show_location_services_confirmation_open_label, object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        this@TrackRecorderActivityPresenter.view.startLocationSourceSettingsActivity()
                    }
                }).create().show()
    }

    private fun setupMainFloatingActionButton() {
        this.subscriptions.add(
                this.view.trackrecorderactivity_main_floatingactionbutton.clicks()
                .subscribe {
                    if (this.trackRecorderSession == null) {
                        this.trackRecorderSubscription = this.startNewTrackRecording()
                    } else {
                        this.trackRecorderSession!!.stateChanged.first(TrackRecordingSessionState.Paused)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe { state ->
                                    when (state) {
                                        TrackRecordingSessionState.Running ->
                                            this.trackRecorderSession!!.pauseTracking()
                                        TrackRecordingSessionState.Paused, null ->
                                            this.trackRecorderSession!!.resumeTracking()
                                        TrackRecordingSessionState.LocationServicesUnavailable ->
                                            this.buildAndShowLocationServicesDialog()
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