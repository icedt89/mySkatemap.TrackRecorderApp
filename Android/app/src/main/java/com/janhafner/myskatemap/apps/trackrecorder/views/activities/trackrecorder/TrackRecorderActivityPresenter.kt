package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.support.design.widget.NavigationView
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.jakewharton.rxbinding2.view.clicks
import com.janhafner.myskatemap.apps.trackrecorder.*
import com.janhafner.myskatemap.apps.trackrecorder.io.data.FitnessActivity
import com.janhafner.myskatemap.apps.trackrecorder.io.data.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.services.ICrudRepository
import com.janhafner.myskatemap.apps.trackrecorder.services.ITrackService
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.*
import com.janhafner.myskatemap.apps.trackrecorder.services.userprofile.IUserProfileService
import com.janhafner.myskatemap.apps.trackrecorder.services.userprofile.UserProfile
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.views.INeedFragmentVisibilityInfo
import com.janhafner.myskatemap.apps.trackrecorder.views.userprofile.settings.UserProfileSettingsActivity
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.tracklist.TrackListActivity
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.DashboardTabFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.data.DataTabFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dialogs.DiscardRecordingAlertDialogBuilder
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dialogs.FinishRecordingAlertDialogBuilder
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dialogs.ShowLocationServicesAlertDialogBuilder
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.map.MapTabFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_track_recorder.*
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter


internal final class TrackRecorderActivityPresenter(private val view: TrackRecorderActivity,
                                                    private val trackService: ITrackService,
                                                    private val trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>,
                                                    private val appSettings: IAppSettings,
                                                    private val userProfileService: IUserProfileService) : INeedFragmentVisibilityInfo {
    private val mainFloatingActionButtonSubscriptions: CompositeDisposable = CompositeDisposable()

    private var fragment: Fragment? = null

    private var trackRecorderServiceControllerSubscription: Disposable? = null

    private var trackRecorderSession: ITrackRecordingSession? = null

    private val sessionSubscriptions: CompositeDisposable = CompositeDisposable()

    private val sessionMenuSubscriptions: CompositeDisposable = CompositeDisposable()

    private var finishTrackRecordingMenuItem: MenuItem? = null

    private var discardTrackRecordingMenuItem: MenuItem? = null

    init {
        this.view.setContentView(R.layout.activity_track_recorder)

        val trackRecorderToolbar = this.view.findViewById<Toolbar>(R.id.trackrecorderactivity_toolbar)
        this.view.setSupportActionBar(trackRecorderToolbar)

        val actionBar = this.view.supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu)

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

        // TODO:
        val navigationView = this.view.findViewById<NavigationView>(R.id.trackrecorderactivity_navigation)
        navigationView.setNavigationItemSelectedListener(
                object : NavigationView.OnNavigationItemSelectedListener {
                    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
                        if(menuItem.itemId == R.id.trackrecorderactivity_navigation_drawer_action_settings) {
                            this@TrackRecorderActivityPresenter.view.startActivity(Intent(this@TrackRecorderActivityPresenter.view, UserProfileSettingsActivity::class.java))
                        } else if(menuItem.itemId == R.id.trackrecorderactivity_navigation_drawer_action_trackrecordings) {
                            val intent = Intent(this@TrackRecorderActivityPresenter.view, TrackListActivity::class.java)

                            this@TrackRecorderActivityPresenter.view.startActivity(intent)
                        }

                        return true
                    }
                })


        // Lifecycle unclear! Handling would be better in another place!
        if (this.trackRecorderSession == null && this.trackRecorderServiceController.currentBinder != null) {
            val currentBinder = this.trackRecorderServiceController.currentBinder!!

            if(currentBinder.currentSession != null) {
                // In case the service is already running (indicated by the currentBinder state),
                // the isClientBoundChanged observable will populate "true" by calling this method.
                this.trackRecorderServiceControllerSubscription = this.trackRecorderServiceController.startAndBindService().subscribe {
                    if(it) {
                        this.trackRecorderSession = this.getInitializedSession(currentBinder.currentSession!!)
                    } else {
                        this.uninitializeSession()
                    }
                }
            }
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
        val dateTimeFormatter: DateTimeFormatter = DateTimeFormat.shortDateTime()
        val nameTemplate = this.view.getString(R.string.trackrecorderactivity_presenter_default_new_trackrecording_name_template)

        val trackRecordingName: String = String.format(nameTemplate, dateTimeFormatter.print(DateTime.now()))

        val userProfile = this.userProfileService.getUserProfileOrDefault()
        val result = TrackRecording.start(trackRecordingName, this.appSettings.locationProviderTypeName)
        if (userProfile.isValidForBurnedEnergyCalculation()) {
            result.fitnessActivity = FitnessActivity(userProfile.age!!,
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
            trackRecorderSession.stateChanged.observeOn(AndroidSchedulers.mainThread()).subscribe{
                    currentState ->
                    when (currentState) {
                        TrackRecordingSessionState.Running -> {
                            Toast.makeText(this.view, R.string.trackrecorderactivity_toast_recording_running, Toast.LENGTH_LONG).show()
                        }
                        TrackRecordingSessionState.Paused -> {
                            Toast.makeText(this.view, R.string.trackrecorderactivity_toast_recording_paused, Toast.LENGTH_LONG).show()
                        }
                        TrackRecordingSessionState.LocationServicesUnavailable -> {
                            Toast.makeText(this.view, R.string.trackrecorderactivity_toast_recording_paused, Toast.LENGTH_LONG).show()

                            this.buildAndShowLocationServicesDialog()
                        } else -> {
                            // Nothing happens here, else branch exist only to prevent warning on compile Oo
                        }
                    }

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
        this.mainFloatingActionButtonSubscriptions.clear()

        this.trackRecorderSession = null

        this.showMenuItems(false)

        this.setupMainFloatingActionButton(this.fragment!!, true)
    }

    public fun onMenuOpened(featureId: Int, menu: Menu?): Boolean {
        if(this.sessionMenuSubscriptions.size() == 0) {
            if(this.finishTrackRecordingMenuItem != null) {
                this.sessionMenuSubscriptions.add(this.finishTrackRecordingMenuItem!!.clicks().subscribe{
                    val finishRecordingAlertDialogBuilder = FinishRecordingAlertDialogBuilder(this.view)
                    finishRecordingAlertDialogBuilder.setPositiveButton(R.string.trackrecorderactivity_finish_confirmation_button_yes_label, {
                        _, _ ->
                        this.finishTrackRecording()
                    })

                    finishRecordingAlertDialogBuilder.show()
                })
            }

            if(this.discardTrackRecordingMenuItem != null) {
                this.sessionMenuSubscriptions.add(this.discardTrackRecordingMenuItem!!.clicks().subscribe{
                    val discardRecordingAlertDialogBuilder = DiscardRecordingAlertDialogBuilder(this.view)
                    discardRecordingAlertDialogBuilder.setPositiveButton(R.string.trackrecorderactivity_discard_confirmation_button_yes_label, {
                        _, _ ->
                        this.discardTrackRecording()
                    })

                    discardRecordingAlertDialogBuilder.show()
                })
            }
        }

        return true
    }

    public fun onCreateOptionsMenu(menu: Menu) : Boolean {
        this.view.menuInflater.inflate(R.menu.track_recorder_activity_toolbar_menu, menu)

        if(this.finishTrackRecordingMenuItem == null) {
            this.finishTrackRecordingMenuItem = menu.findItem(R.id.trackrecorderactivity_toolbar_finish_currenttracking)
        }

        if(this.discardTrackRecordingMenuItem == null) {
            this.discardTrackRecordingMenuItem = menu.findItem(R.id.trackrecorderactivity_toolbar_discard_currenttracking)
        }

        return true
    }

    public fun destroy() {
        this.trackRecorderServiceController.unbindService()

        this.trackRecorderServiceControllerSubscription?.dispose()

        this.uninitializeSession()

        this.sessionSubscriptions.dispose()
    }

    private fun finishTrackRecording() {
        // TODO: Make something useful with it (history, upload etc...)
        val trackRecording = this.trackRecorderSession!!.finishTracking()

        this.uninitializeSession()
    }

    private fun discardTrackRecording() {
        this.trackRecorderSession!!.discardTracking()

        this.uninitializeSession()
    }

    private fun buildAndShowLocationServicesDialog() {
        ShowLocationServicesAlertDialogBuilder(this.view)
                .setPositiveButton(R.string.trackrecorderactivity_show_location_services_confirmation_open_label,  object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        this@TrackRecorderActivityPresenter.view.startLocationSourceSettingsActivity()
                    }
                }).create().show()
    }

    private fun setupMainFloatingActionButton(fragment: Fragment, isVisibleToUser: Boolean){
        this.mainFloatingActionButtonSubscriptions.clear()
        this.fragment = fragment

        if(this.trackRecorderSession == null) {
            this.mainFloatingActionButtonSubscriptions.add (
                this.view.trackrecorderactivity_main_floatingactionbutton.clicks()
                        .subscribe {
                            this.trackRecorderServiceControllerSubscription = this.trackRecorderServiceController.startAndBindService().subscribe {
                                if(it) {
                                    val binder = this.trackRecorderServiceController.currentBinder!!

                                    val uninitializedSession: ITrackRecordingSession

                                    var currentTrackRecording: TrackRecording? = null
                                    if (this.appSettings.currentTrackRecordingId != null) {
                                        try {
                                            currentTrackRecording = this.trackService.getByIdOrNull(this.appSettings.currentTrackRecordingId!!.toString())
                                        } catch(exception: Exception) {
                                            // TODO
                                            Log.e("RecorderPresenter", "Could not load track recording (Id=\"${this.appSettings.currentTrackRecordingId!!}\")!${exception}")
                                        }
                                    }

                                    var createNewTrackRecording: Boolean = false
                                    if (binder.currentSession == null) {
                                        if (currentTrackRecording == null) {
                                            val newTrackRecording = this.createNewTrackRecording()

                                            uninitializedSession = binder.useTrackRecording(newTrackRecording)

                                            createNewTrackRecording = true
                                        } else {
                                            uninitializedSession = binder.useTrackRecording(currentTrackRecording)
                                        }
                                    } else {
                                        uninitializedSession = binder.currentSession!!
                                    }

                                    this.trackRecorderSession = this.getInitializedSession(uninitializedSession)

                                    if(createNewTrackRecording && this.view.isLocationServicesEnabled()){
                                        this.trackRecorderSession!!.resumeTracking()
                                    }

                                    if(this.fragment != null) {
                                        this.setupMainFloatingActionButton(this.fragment!!, true)
                                    }
                                } else {
                                    this.uninitializeSession()
                                }
                            }
                        }
            )
        } else if(isVisibleToUser) {
            if(fragment is DataTabFragment || fragment is MapTabFragment || fragment is DashboardTabFragment) {
                this.mainFloatingActionButtonSubscriptions.addAll(
                        this.trackRecorderSession!!.stateChanged
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe {
                                    var iconId = R.drawable.ic_action_track_recorder_recording_pause
                                    if (it == TrackRecordingSessionState.Paused) {
                                        iconId = R.drawable.ic_action_track_recorder_recording_startresume
                                    } else if (it == TrackRecordingSessionState.LocationServicesUnavailable) {
                                        iconId = R.drawable.ic_location
                                    }

                                    this.view.trackrecorderactivity_main_floatingactionbutton.setImageResource(iconId)
                                },

                        this.view.trackrecorderactivity_main_floatingactionbutton.clicks()
                                .subscribe {
                                    this.trackRecorderSession!!.stateChanged.first(TrackRecordingSessionState.Paused).subscribe {
                                        state ->
                                        when(state) {
                                            TrackRecordingSessionState.Running -> {
                                                this.trackRecorderSession!!.pauseTracking()
                                            }
                                            TrackRecordingSessionState.Paused -> {
                                                this.trackRecorderSession!!.resumeTracking()
                                            }
                                            TrackRecordingSessionState.LocationServicesUnavailable -> {
                                                this.buildAndShowLocationServicesDialog()
                                            }
                                        }
                                    }
                                }
                )
            }
        }
    }

    public fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == 42 && resultCode == Activity.RESULT_OK) {
            val contentInfo = this.view.contentResolver.getContentInfo(data!!.data)
            val o = contentInfo
            val p = o
        }
    }

    public override fun onFragmentVisibilityChange(fragment: Fragment, isVisibleToUser: Boolean) {
        this.setupMainFloatingActionButton(fragment, isVisibleToUser)
    }
}