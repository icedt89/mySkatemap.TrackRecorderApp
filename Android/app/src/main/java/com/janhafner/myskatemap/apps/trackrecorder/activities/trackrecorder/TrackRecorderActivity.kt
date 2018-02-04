package com.janhafner.myskatemap.apps.trackrecorder.activities.trackrecorder

import android.Manifest
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.view.enabled
import com.jakewharton.rxbinding2.view.visibility
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.location.TrackRecorderServiceState
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import io.reactivex.disposables.CompositeDisposable

internal final class TrackRecorderActivity: AppCompatActivity() {
    private val subscriptions: CompositeDisposable = CompositeDisposable()

    private var optionsMenuSubscriptions: CompositeDisposable? = null

    private val mapSubscriptions : CompositeDisposable = CompositeDisposable()

    private var viewModel: TrackRecorderActivityViewModel? = null

    private var finishCurrentTrackRecordingMenuItem: MenuItem? = null

    private var discardCurrentTrackRecordingMenuItem: MenuItem? = null

    private lateinit var startRecordingFloatingActionButton: FloatingActionButton

    private lateinit var pauseRecordingFloatingActionButton: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.setContentView(R.layout.activity_track_recorder)

        val trackRecorderToolbar = this.findViewById<Toolbar>(R.id.trackrecorderactivity_toolbar)
        this.setSupportActionBar(trackRecorderToolbar)

        val viewPager = this.findViewById<ViewPager>(R.id.trackrecorderactivity_toolbar_viewpager)
        viewPager.adapter = TrackRecorderTabsAdapter(this, this.supportFragmentManager)

        val tabLayout = this.findViewById<TabLayout>(R.id.trackrecorderactivity_toolbar_tablayout)
        tabLayout.setupWithViewPager(viewPager)

        this.startRecordingFloatingActionButton = this.findViewById<FloatingActionButton>(R.id.trackrecorderactivity_startrecording_floatingactionbutton)
        this.pauseRecordingFloatingActionButton = this.findViewById<FloatingActionButton>(R.id.trackrecorderactivity_pauserecording_floatingactionbutton)

        this.viewModel = TrackRecorderActivityViewModel(this)
        this.viewModel!!.startAndBindService()
    }

    public override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menuInflater.inflate(R.menu.track_recorder_activity_toolbar_menu, menu)

        this.discardCurrentTrackRecordingMenuItem = menu.findItem(R.id.trackrecorderactivity_toolbar_discard_currenttrackrecording_menuitem)
        this.finishCurrentTrackRecordingMenuItem = menu.findItem(R.id.trackrecorderactivity_toolbar_finish_currenttrackrecording_menuitem)

        this.subscribeToOptionsMenu()

        return true
    }

    public override fun onAttachFragment(fragment: android.support.v4.app.Fragment?) {
        super.onAttachFragment(fragment)

        val trackRecorderActivityDependantFragment = fragment as ITrackRecorderActivityDependantFragment
        if(trackRecorderActivityDependantFragment == null) {
            return
        }

        trackRecorderActivityDependantFragment.setViewModel(this.viewModel!!)
    }

    public override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        this.viewModel?.trackSessionStateChanged!!.first(TrackRecorderServiceState.Initializing)!!.subscribe{
            currentState ->
                if(currentState != TrackRecorderServiceState.Initializing) {
                    this.viewModel?.saveCurrentRecording()
                }
        }.dispose()
    }

    override fun onResume() {
        super.onResume()

        this.subscribeToViewModel()
        this.subscribeToOptionsMenu()
    }

    override fun onPause() {
        super.onPause()

        this.mapSubscriptions.clear()
        this.subscriptions.clear()
        this.optionsMenuSubscriptions?.clear()
        this.optionsMenuSubscriptions = null
    }

    override fun onDestroy() {
        super.onDestroy()

        this.viewModel!!.unbindService()

        this.mapSubscriptions.clear()
        this.subscriptions.clear()
        this.optionsMenuSubscriptions?.clear()
        this.optionsMenuSubscriptions = null
    }

    private fun subscribeToViewModel() {
        this.subscriptions.addAll(
                this.viewModel!!.canStartResumeRecordingChanged.subscribe(this.startRecordingFloatingActionButton.visibility()),
                this.viewModel!!.canStartResumeRecordingChanged.subscribe(this.startRecordingFloatingActionButton.enabled()),
                this.startRecordingFloatingActionButton.clicks().subscribe({
                    Dexter.withActivity(this)
                            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                            .withListener(object: PermissionListener {
                                override final fun onPermissionGranted(response: PermissionGrantedResponse) {
                                    Log.d("TrackRecorderActivity", "Permission for ACCESS_FINE_LOCATION is granted")
                                    this@TrackRecorderActivity.viewModel!!.startResumeRecording()
                                }

                                override final fun onPermissionDenied(response: PermissionDeniedResponse) {
                                    Log.d("TrackRecorderActivity", "Permission for ACCESS_FINE_LOCATION is denied")
                                }

                                override final fun onPermissionRationaleShouldBeShown(permission: PermissionRequest, token: PermissionToken) {
                                    Log.d("TrackRecorderActivity", "Permission Rationale should be shown")
                                }
                            })
                            .check()
                }),

                this.viewModel!!.canPauseRecordingChanged.subscribe(this.pauseRecordingFloatingActionButton.visibility()),
                this.viewModel!!.canPauseRecordingChanged.subscribe(this.pauseRecordingFloatingActionButton.enabled()),
                this.pauseRecordingFloatingActionButton.clicks().subscribe({
                    this.viewModel!!.pauseRecording()
                }),

                this.viewModel!!.trackSessionStateChanged.subscribe{
                    currentState ->
                    when (currentState) {
                        TrackRecorderServiceState.Running ->
                            Toast.makeText(this, R.string.trackrecorderactivity_toast_recording_running, Toast.LENGTH_LONG).show()
                        TrackRecorderServiceState.Paused,
                        TrackRecorderServiceState.LocationServicesUnavailable ->
                            Toast.makeText(this, R.string.trackrecorderactivity_toast_recording_paused, Toast.LENGTH_LONG).show()
                    }
                }
        )
    }

    private fun subscribeToOptionsMenu() {
        if(this.optionsMenuSubscriptions != null ||
                (this.finishCurrentTrackRecordingMenuItem == null
                      || discardCurrentTrackRecordingMenuItem == null)) {
            return
        }

        this.optionsMenuSubscriptions = CompositeDisposable()
        this.optionsMenuSubscriptions!!.addAll(
                this.viewModel!!.canFinishRecordingChanged.subscribe(this.finishCurrentTrackRecordingMenuItem!!.enabled()),
                this.finishCurrentTrackRecordingMenuItem!!.clicks().subscribe({
                    this.viewModel!!.finishRecording()
                }),

                this.viewModel!!.canDiscardRecordingChanged.subscribe(this.discardCurrentTrackRecordingMenuItem!!.enabled()),
                this.discardCurrentTrackRecordingMenuItem!!.clicks().subscribe({
                    this.viewModel!!.discardRecording()
                })
        )
    }
}