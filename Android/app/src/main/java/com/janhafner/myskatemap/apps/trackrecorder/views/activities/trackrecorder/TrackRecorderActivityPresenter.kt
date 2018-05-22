package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder

import android.app.Activity
import android.content.Context
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
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.getContentInfo
import com.janhafner.myskatemap.apps.trackrecorder.io.data.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.isLocationServicesEnabled
import com.janhafner.myskatemap.apps.trackrecorder.services.ITrackService
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.ITrackRecordingSession
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.ServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceState
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.views.INeedFragmentVisibilityInfo
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.settings.SettingsActivity
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.tracklist.TrackListActivity
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.attachments.AttachmentsTabFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.data.DataTabFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dialogs.DiscardRecordingAlertDialogBuilder
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dialogs.FinishRecordingAlertDialogBuilder
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.map.MapTabFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_track_recorder.*
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter


internal final class TrackRecorderActivityPresenter(private val trackRecorderActivity: TrackRecorderActivity,
                                                    private val trackService: ITrackService,
                                                    private val trackRecorderServiceController: ServiceController<TrackRecorderServiceBinder>,
                                                    private val appSettings: IAppSettings) : INeedFragmentVisibilityInfo {
    private val mainFloatingActionButtonSubscriptions: CompositeDisposable = CompositeDisposable()

    private var fragment: Fragment? = null

    private val trackRecorderServiceControllerSubscription: Disposable

    private var trackRecorderSession: ITrackRecordingSession? = null

    private val sessionSubscriptions: CompositeDisposable = CompositeDisposable()

    private var finishTrackRecordingMenuItem: MenuItem? = null

    private var discardTrackRecordingMenuItem: MenuItem? = null

    init {
        this.trackRecorderActivity.setContentView(R.layout.activity_track_recorder)

        val trackRecorderToolbar = this.trackRecorderActivity.findViewById<Toolbar>(R.id.trackrecorderactivity_toolbar)
        this.trackRecorderActivity.setSupportActionBar(trackRecorderToolbar)

        val actionBar = this.trackRecorderActivity.supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu)

        val viewPager = this.trackRecorderActivity.findViewById<ViewPager>(R.id.trackrecorderactivity_toolbar_viewpager)
        viewPager.adapter = TrackRecorderTabsAdapter(this.trackRecorderActivity, this.trackRecorderActivity.supportFragmentManager)
        viewPager.offscreenPageLimit = viewPager.adapter!!.count

        val tabLayout = this.trackRecorderActivity.findViewById<TabLayout>(R.id.trackrecorderactivity_toolbar_tablayout)
        tabLayout.setupWithViewPager(viewPager)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                val self = this@TrackRecorderActivityPresenter.trackRecorderActivity

                if (self.currentFocus == null) {
                    return
                }

                val inputMethodManager = trackRecorderActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(self.currentFocus?.windowToken, 0)
            }
        })

        // TODO:
        val navigationView = this.trackRecorderActivity.findViewById<NavigationView>(R.id.trackrecorderactivity_navigation)
        navigationView.setNavigationItemSelectedListener(
                object : NavigationView.OnNavigationItemSelectedListener {
                    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
                        if(menuItem.itemId == R.id.trackrecorderactivity_navigation_drawer_action_settings) {
                            this@TrackRecorderActivityPresenter.trackRecorderActivity.startActivity(Intent(this@TrackRecorderActivityPresenter.trackRecorderActivity, SettingsActivity::class.java))
                        } else if(menuItem.itemId == R.id.trackrecorderactivity_navigation_drawer_action_trackrecordings) {
                            val intent = Intent(this@TrackRecorderActivityPresenter.trackRecorderActivity, TrackListActivity::class.java)

                            this@TrackRecorderActivityPresenter.trackRecorderActivity.startActivity(intent)
                        }

                        return true
                    }
                })

        this.trackRecorderServiceControllerSubscription = this.trackRecorderServiceController.startAndBindService().subscribe{
            if(it) {
                val binder = this.trackRecorderServiceController.currentBinder!!

                val uninitializedSession: ITrackRecordingSession

                var currentTrackRecording: TrackRecording? = null
                if (this.appSettings.currentTrackRecordingId != null) {
                    try {
                        currentTrackRecording = this.trackService.getTrackRecording(this.appSettings.currentTrackRecordingId!!.toString())
                    } catch(exception: Throwable) {
                        // TODO
                        Log.e("RecorderPresenter", "Could not load track recording (Id=\"${this.appSettings.currentTrackRecordingId!!}\")!${exception}")
                    }
                }

                val createNewTrackRecording: Boolean
                if(binder.currentSession == null) {
                    val mode = ActivityStartMode.valueOf(this.trackRecorderActivity.intent.getStringExtra(TrackRecorderActivityPresenter.ACTIVITY_START_MODE_KEY))
                    createNewTrackRecording = mode == ActivityStartMode.StartNew || (mode == ActivityStartMode.TryResume && currentTrackRecording == null)
                    if (createNewTrackRecording) {
                        val newTrackRecording = this.createNewTrackRecording()

                        uninitializedSession = binder.useTrackRecording(newTrackRecording)
                    } else {
                        uninitializedSession = binder.useTrackRecording(currentTrackRecording!!)
                    }
                } else {
                    uninitializedSession = binder.currentSession!!

                    createNewTrackRecording = false
                }

                this.trackRecorderSession = this.getInitializedSession(uninitializedSession)

                if(createNewTrackRecording){
                    if (this.trackRecorderActivity.isLocationServicesEnabled()) {
                        this.trackRecorderSession!!.resumeTracking()
                    } else {
                        ShowLocationServicesSnackbar.make(this.trackRecorderActivity, this.trackRecorderActivity.currentFocus).show()
                    }
                }

                if(this.fragment != null) {
                    this.setupMainFloatingActionButton(this.fragment!!, true)
                }
            } else {
                this.uninitializeSession()
            }
        }
    }

    private fun createNewTrackRecording(): TrackRecording {
        val dateTimeFormatter: DateTimeFormatter = DateTimeFormat.shortDateTime()
        val nameTemplate = this.trackRecorderActivity.getString(R.string.trackrecorderactivity_presenter_default_new_trackrecording_name_template)

        val trackRecordingName: String = String.format(nameTemplate, dateTimeFormatter.print(DateTime.now()))

        return TrackRecording.start(trackRecordingName, this.appSettings.locationProviderTypeName)
    }

    private fun getInitializedSession(trackRecorderSession: ITrackRecordingSession): ITrackRecordingSession {
        this.sessionSubscriptions.addAll(
            trackRecorderSession.stateChanged.observeOn(AndroidSchedulers.mainThread()).subscribe{
                    currentState ->
                    when (currentState) {
                        TrackRecorderServiceState.Running -> {
                            Toast.makeText(this.trackRecorderActivity, R.string.trackrecorderactivity_toast_recording_running, Toast.LENGTH_LONG).show()
                        }
                        TrackRecorderServiceState.Paused,
                        TrackRecorderServiceState.LocationServicesUnavailable ->
                            Toast.makeText(this.trackRecorderActivity, R.string.trackrecorderactivity_toast_recording_paused, Toast.LENGTH_LONG).show()
                        else -> {
                            // Nothing happens here. Else branch exist only to prevent warning on compile Oo
                        }
                    }

                    if(this.discardTrackRecordingMenuItem != null) {
                        this.discardTrackRecordingMenuItem!!.isEnabled = currentState != TrackRecorderServiceState.Running
                    }

                    if(this.finishTrackRecordingMenuItem != null) {
                        this.finishTrackRecordingMenuItem!!.isEnabled = currentState != TrackRecorderServiceState.Running
                    }
                }
        )

        return trackRecorderSession
    }

    private fun uninitializeSession() {
        this.sessionSubscriptions.clear()
        this.mainFloatingActionButtonSubscriptions.clear()

        this.trackRecorderSession = null
    }

    public fun onCreateOptionsMenu(menu: Menu) : Boolean {
        this.trackRecorderActivity.menuInflater.inflate(R.menu.track_recorder_activity_toolbar_menu, menu)

        if(this.finishTrackRecordingMenuItem == null) {
            this.finishTrackRecordingMenuItem = menu.findItem(R.id.trackrecorderactivity_toolbar_finish_currenttracking)

            this.sessionSubscriptions.add(this.finishTrackRecordingMenuItem!!.clicks().subscribe{
                val finishRecordingAlertDialogBuilder = FinishRecordingAlertDialogBuilder(this.trackRecorderActivity)
                finishRecordingAlertDialogBuilder.setPositiveButton(R.string.trackrecorderactivity_finish_confirmation_button_yes_label, {
                    _,
                    _ ->
                    // TODO: Make something useful with it (history, upload etc...)
                    val trackRecording = this.trackRecorderSession!!.finishTracking()

                    this.uninitializeSession()

                    this.trackRecorderActivity.startActivity(Intent(this.trackRecorderActivity, TrackListActivity::class.java))

                    this.trackRecorderActivity.finish()
                })

                finishRecordingAlertDialogBuilder.show()
            })
        }

        if(this.discardTrackRecordingMenuItem == null) {
            this.discardTrackRecordingMenuItem = menu.findItem(R.id.trackrecorderactivity_toolbar_discard_currenttracking)

            this.sessionSubscriptions.add(this.discardTrackRecordingMenuItem!!.clicks().subscribe{
                        val discardRecordingAlertDialogBuilder = DiscardRecordingAlertDialogBuilder(this.trackRecorderActivity)
                        discardRecordingAlertDialogBuilder.setPositiveButton(R.string.trackrecorderactivity_discard_confirmation_button_yes_label, {
                            _, _ ->
                            this.trackRecorderSession!!.discardTracking()

                            this.uninitializeSession()

                            this.trackRecorderActivity.startActivity(Intent(this.trackRecorderActivity, TrackListActivity::class.java))

                            this.trackRecorderActivity.finish()
                        })

                        discardRecordingAlertDialogBuilder.show()
                    })
        }

        return true
    }

    public fun destroy() {
        this.trackRecorderServiceController.unbindService()

        this.trackRecorderServiceControllerSubscription.dispose()

        this.uninitializeSession()
    }

    fun save() {
        this.trackRecorderSession?.stateChanged?.last(TrackRecorderServiceState.Idle)!!.subscribe{
            it ->
            if(it != TrackRecorderServiceState.Idle) {
                this.trackRecorderSession!!.saveTracking()
            }
        }
    }

    private fun setupMainFloatingActionButton(fragment: Fragment, isVisibleToUser: Boolean){
        this.mainFloatingActionButtonSubscriptions.clear()
        this.fragment = fragment

        if(this.trackRecorderSession != null && isVisibleToUser) {
            if(fragment is DataTabFragment || fragment is MapTabFragment) {
                this.mainFloatingActionButtonSubscriptions.addAll(
                        this.trackRecorderSession!!.stateChanged
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe {
                                    this.trackRecorderActivity.trackrecorderactivity_main_floatingactionbutton.isEnabled = it == TrackRecorderServiceState.Running || it == TrackRecorderServiceState.Paused

                                    var iconId = R.drawable.ic_action_track_recorder_recording_startresume
                                    if (it == TrackRecorderServiceState.Running) {
                                        iconId = R.drawable.ic_action_track_recorder_recording_pause
                                    }

                                    this.trackRecorderActivity.trackrecorderactivity_main_floatingactionbutton.setImageResource(iconId)
                                },

                        this.trackRecorderActivity.trackrecorderactivity_main_floatingactionbutton.clicks()
                                .subscribe {
                                    this.trackRecorderSession!!.stateChanged.first(TrackRecorderServiceState.Idle).subscribe {
                                        state ->
                                        if(state == TrackRecorderServiceState.Paused) {
                                            if (this.trackRecorderActivity.isLocationServicesEnabled()) {
                                                this.trackRecorderSession!!.resumeTracking()
                                            } else {
                                                ShowLocationServicesSnackbar.make(this.trackRecorderActivity, this.trackRecorderActivity.currentFocus).show()
                                            }
                                        } else {
                                            this.trackRecorderSession!!.pauseTracking()
                                        }
                                    }
                                }
                )
            } else if(fragment is AttachmentsTabFragment) {
                this.trackRecorderActivity.trackrecorderactivity_main_floatingactionbutton.setImageResource(R.drawable.ic_action_track_recorder_attachments_addfromlibrary)

                this.mainFloatingActionButtonSubscriptions.addAll(
                        this.trackRecorderActivity.trackrecorderactivity_main_floatingactionbutton.clicks()
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe {
                                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                                    intent.type = "image/*"

                                    this.trackRecorderActivity.startActivityForResult(intent, 42)
                                }
                )
            }
        }
    }

    public fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == 42 && resultCode == Activity.RESULT_OK) {
            val contentInfo = this.trackRecorderActivity.contentResolver.getContentInfo(data!!.data)
            val o = contentInfo
            val p = o
        }
    }

    public override fun onFragmentVisibilityChange(fragment: Fragment, isVisibleToUser: Boolean) {
        this.setupMainFloatingActionButton(fragment, isVisibleToUser)
    }

    companion object {
        public const val ACTIVITY_START_MODE_KEY: String = "ActivityStartMode"
    }
}