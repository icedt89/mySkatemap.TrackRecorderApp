package com.janhafner.myskatemap.apps.trackrecorder.activities

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.jakewharton.rxbinding2.view.RxMenuItem
import com.jakewharton.rxbinding2.view.RxView
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.consume
import com.janhafner.myskatemap.apps.trackrecorder.location.TrackRecorderServiceState
import com.janhafner.myskatemap.apps.trackrecorder.map.ITrackRecorderMap
import com.janhafner.myskatemap.apps.trackrecorder.map.TrackRecorderMap
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

internal final class TrackRecorderActivity : AppCompatActivity(), OnMapReadyCallback {
    private val subscriptions: CompositeDisposable = CompositeDisposable()

    private val optionsMenuSubscriptions: CompositeDisposable = CompositeDisposable()

    private var viewModel: TrackRecorderActivityViewModel? = null

    private var locationsChangedAvailableSubscription : Disposable? = null

    private var currentLocationsChangedObservable : Disposable? = null

    private var trackRecorderMap: ITrackRecorderMap? = null

    private var finishCurrentTrackRecordingMenuItem: MenuItem? = null

    private var discardCurrentTrackRecordingMenuItem : MenuItem? = null

    private var showCurrentTrackRecordingAttachments: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.setContentView(R.layout.activity_track_recorder)

        val trackRecorderToolbar = this.findViewById<Toolbar>(R.id.trackrecorderactivity_toolbar)
        this.setSupportActionBar(trackRecorderToolbar)

        this.initializeGoogleMap()

        this.viewModel = TrackRecorderActivityViewModel(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menuInflater.inflate(R.menu.track_recorder_activity_toolbar_menu, menu)

        this.showCurrentTrackRecordingAttachments = menu.findItem(R.id.trackrecorderactivity_toolbar_attachments_currenttrackrecording_menuitem)
        this.discardCurrentTrackRecordingMenuItem = menu.findItem(R.id.trackrecorderactivity_toolbar_discard_currenttrackrecording_menuitem)
        this.finishCurrentTrackRecordingMenuItem = menu.findItem(R.id.trackrecorderactivity_toolbar_finish_currenttrackrecording_menuitem)

        this.subscribeOptionsMenuToViewModel()

        return true
    }

    override fun onResume() {
        super.onResume()

        this.subscribeToViewModel()
    }

    override fun onPause() {
        super.onPause()

    }

    override fun onDestroy() {
        super.onDestroy()

        this.stopService(Intent(this, com.janhafner.myskatemap.apps.trackrecorder.location.TrackRecorderService::class.java))
    }

    private fun initializeGoogleMap() {
        val mapFragment = this.fragmentManager.findFragmentById(R.id.trackrecorderactivity_googlemap_mapfragment) as MapFragment

        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        // Zoom to awesome Kackstadt :)
        this.trackRecorderMap = TrackRecorderMap.fromGoogleMap(googleMap, LatLng(50.8357, 12.92922), 13f)
    }

    private fun subscribeToViewModel() {
        val startRecordingFloatingActionButton = this.findViewById<FloatingActionButton>(R.id.trackrecorderactivity_startrecording_floatingactionbutton)
        this.subscriptions.addAll(
                this.viewModel!!.canStartResumeRecordingChanged.subscribe(RxView.visibility(startRecordingFloatingActionButton)),
                this.viewModel!!.canStartResumeRecordingChanged.subscribe(RxView.enabled(startRecordingFloatingActionButton)),
                RxView.clicks(startRecordingFloatingActionButton).subscribe({
                    Dexter.withActivity(this)
                            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                            .withListener(object : PermissionListener {
                                override final fun onPermissionGranted(response: PermissionGrantedResponse) {
                                    Log.d("TrackRecorderActivity", "Permission for ACCESS_FINE_LOCATION is granted")
                                }

                                override final fun onPermissionDenied(response: PermissionDeniedResponse) {
                                    Log.d("TrackRecorderActivity", "Permission for ACCESS_FINE_LOCATION is denied")
                                }

                                override final fun onPermissionRationaleShouldBeShown(permission: PermissionRequest, token: PermissionToken) {
                                    Log.d("TrackRecorderActivity", "Permission Rationale should be shown")
                                }
                            })
                            .onSameThread()
                            .check()

                    this.viewModel!!.startResumeRecording()
                })
        )

        val pauseRecordingFloatingActionButton = this.findViewById<FloatingActionButton>(R.id.trackrecorderactivity_pauserecording_floatingactionbutton)
        this.subscriptions.addAll(
                this.viewModel!!.canPauseRecordingChanged.subscribe(RxView.visibility(pauseRecordingFloatingActionButton)),
                this.viewModel!!.canPauseRecordingChanged.subscribe(RxView.enabled(pauseRecordingFloatingActionButton)),
                RxView.clicks(pauseRecordingFloatingActionButton).subscribe({
                    this.viewModel!!.pauseRecording()
                })
        )

        this.subscriptions.addAll(
                this.viewModel!!.trackSessionStateChanged.subscribe{
                    currentState ->
                    when (currentState) {
                        TrackRecorderServiceState.Running ->
                            Toast.makeText(this, R.string.trackrecordersactivity_toast_recording_running, Toast.LENGTH_LONG).show()
                        TrackRecorderServiceState.Paused,
                        TrackRecorderServiceState.LocationServicesUnavailable ->
                            Toast.makeText(this, R.string.trackrecordersactivity_toast_recording_paused, Toast.LENGTH_LONG).show()
                    }
                }
        )

        this.subscribeGoogleMapToLocationUpdates()
    }

    private fun subscribeGoogleMapToLocationUpdates() {
        this.locationsChangedAvailableSubscription = this.viewModel!!.locationsChangedAvailable.subscribe{
            locationsChangedObservable ->
                if(this.currentLocationsChangedObservable != null) {
                    this.currentLocationsChangedObservable!!.dispose()
                }

                if(this.trackRecorderMap != null) {
                    this.currentLocationsChangedObservable = locationsChangedObservable
                            .observeOn(AndroidSchedulers.mainThread())
                            .buffer(5, TimeUnit.SECONDS, AndroidSchedulers.mainThread(), 5)
                            .subscribe(this.trackRecorderMap!!.consume())
                }
        }
    }

    private fun subscribeOptionsMenuToViewModel() {
        this.optionsMenuSubscriptions.addAll(
                this.viewModel!!.canFinishRecordingChanged.subscribe(RxMenuItem.enabled(this.finishCurrentTrackRecordingMenuItem!!)),
                RxMenuItem.clicks(this.finishCurrentTrackRecordingMenuItem!!).subscribe({
                    this.viewModel!!.finishRecording()
                })
        )

        this.optionsMenuSubscriptions.addAll(
                this.viewModel!!.canDiscardRecordingChanged.subscribe(RxMenuItem.enabled(this.discardCurrentTrackRecordingMenuItem!!)),
                RxMenuItem.clicks(this.discardCurrentTrackRecordingMenuItem!!).subscribe({
                    this.viewModel!!.discardRecording()
                })
        )
    }
}