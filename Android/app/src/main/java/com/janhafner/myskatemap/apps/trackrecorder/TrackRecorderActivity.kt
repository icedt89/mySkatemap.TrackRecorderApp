package com.janhafner.myskatemap.apps.trackrecorder

import android.Manifest
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.jakewharton.rxbinding2.support.v4.widget.RxSwipeRefreshLayout
import com.jakewharton.rxbinding2.view.RxMenuItem
import com.jakewharton.rxbinding2.view.RxView
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

internal final class TrackRecorderActivity : AppCompatActivity(), OnMapReadyCallback {
    private val subscriptions: CompositeDisposable = CompositeDisposable()

    private val optionsMenuSubscriptions: CompositeDisposable = CompositeDisposable()

    private var foregroundTrackRecorderMapLocationSubscription : Disposable? = null

    private var viewModelReadySubscription : Disposable? = null

    private var viewModel: TrackRecorderActivityViewModel? = null

    private var trackRecorderMap: ITrackRecorderMap? = null

    private var finishCurrentTrackRecordingMenuItem: MenuItem? = null

    private var discardCurrentTrackRecordingMenuItem : MenuItem? = null

    private var refreshViewMenuItem : MenuItem? = null

    private var showCurrentTrackRecordingAttachments: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.setContentView(R.layout.activity_track_recorder)

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(object : PermissionListener {
                    override final fun onPermissionGranted(response: PermissionGrantedResponse) {

                    }

                    override final fun onPermissionDenied(response: PermissionDeniedResponse) {

                    }

                    override final fun onPermissionRationaleShouldBeShown(permission: PermissionRequest, token: PermissionToken) {

                    }
                })
                .onSameThread()
                .check()

        val trackRecorderToolbar = this.findViewById<Toolbar>(R.id.trackrecorderactivity_toolbar)
        this.setSupportActionBar(trackRecorderToolbar)

        this.initializeGoogleMap()

        this.viewModel = TrackRecorderActivityViewModel(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menuInflater.inflate(R.menu.track_recorder_activity_toolbar_menu, menu)

        this.refreshViewMenuItem = menu.findItem(R.id.trackrecorderactivity_toolbar_refresh_view_menuitem)
        this.showCurrentTrackRecordingAttachments = menu.findItem(R.id.trackrecorderactivity_toolbar_attachments_currenttrackrecording_menuitem)
        this.discardCurrentTrackRecordingMenuItem = menu.findItem(R.id.trackrecorderactivity_toolbar_discard_currenttrackrecording_menuitem)
        this.finishCurrentTrackRecordingMenuItem = menu.findItem(R.id.trackrecorderactivity_toolbar_finish_currenttrackrecording_menuitem)

        this.subscribeOptionsMenuToViewModel()

        return true
    }

    private fun refreshTrackPathOnMap() {
        this.viewModel!!.saveCurrentTrackRecording()
    }

    override fun onStart() {
        super.onStart()

        this.subscribeToViewModel()
    }

    private fun subscribeToViewModel() {
        val startRecordingFloatingActionButton = this.findViewById<FloatingActionButton>(R.id.trackrecorderactivity_startrecording_floatingactionbutton)
        this.subscriptions.addAll(
                this.viewModel!!.canStartRecording.subscribe(RxView.visibility(startRecordingFloatingActionButton)),
                this.viewModel!!.canStartRecording.subscribe(RxView.enabled(startRecordingFloatingActionButton)),
                RxView.clicks(startRecordingFloatingActionButton).subscribe({
                    this.viewModel!!.startRecording()
                })
        )

        val pauseRecordingFloatingActionButton = this.findViewById<FloatingActionButton>(R.id.trackrecorderactivity_pauserecording_floatingactionbutton)
        this.subscriptions.addAll(
                this.viewModel!!.canPauseRecording.subscribe(RxView.visibility(pauseRecordingFloatingActionButton)),
                this.viewModel!!.canPauseRecording.subscribe(RxView.enabled(pauseRecordingFloatingActionButton)),
                RxView.clicks(pauseRecordingFloatingActionButton).subscribe({
                    this.viewModel!!.pauseRecording()
                })
        )

        val googleMapSwipeRefresh = this.findViewById<SwipeRefreshLayout>(R.id.trackrecorderactivity_swiperefresh_map)
        this.subscriptions.add(RxSwipeRefreshLayout.refreshes(googleMapSwipeRefresh).subscribe({
            this.refreshTrackPathOnMap()

            googleMapSwipeRefresh.isRefreshing = false
        }))

        this.subscribeGoogleMapToLocationUpdates()
    }

    private fun subscribeGoogleMapToLocationUpdates() {
        this.viewModelReadySubscription = this.viewModel!!.isReady.subscribe {
            isReady: Boolean ->
            if(isReady) {
                this.subscriptions.add(this.viewModel!!.trackRecorderServiceStateChanged.subscribe{
                    state: TrackRecorderServiceState ->
                        if(state == TrackRecorderServiceState.Running) {
                            if(this.foregroundTrackRecorderMapLocationSubscription != null) {
                                Log.w("TrackRecorderActivity", "foregroundTrackRecorderMapLocationSubscription NOT NULL")

                                if(!this.foregroundTrackRecorderMapLocationSubscription!!.isDisposed) {
                                    Log.w("TrackRecorderActivity", "foregroundTrackRecorderMapLocationSubscription NOT DISPOSED")
                                }
                            }
                            this.foregroundTrackRecorderMapLocationSubscription = this.viewModel!!.locations.observeOn(AndroidSchedulers.mainThread()).subscribe(this.trackRecorderMap!!.consume())
                        } else {
                            this.foregroundTrackRecorderMapLocationSubscription?.dispose()
                        }
                })
            } else {
                this.viewModelReadySubscription?.dispose()
            }
        }
    }

    private fun subscribeOptionsMenuToViewModel() {
        this.optionsMenuSubscriptions.addAll(
                // this.viewModel!!.canFinishCurrentTrackRecording.subscribe(RxMenuItem.enabled(this.refreshViewMenuItem!!)),
                RxMenuItem.clicks(this.refreshViewMenuItem!!).subscribe({
                    this.refreshTrackPathOnMap()
                })
        )

        this.optionsMenuSubscriptions.addAll(
                this.viewModel!!.canFinishCurrentTrackRecording.subscribe(RxMenuItem.enabled(this.finishCurrentTrackRecordingMenuItem!!)),
                RxMenuItem.clicks(this.finishCurrentTrackRecordingMenuItem!!).subscribe({
                    this.viewModel!!.finishCurrentTrackRecording()
                })
        )

        this.optionsMenuSubscriptions.addAll(
                this.viewModel!!.canDiscardCurrentTrackRecording.subscribe(RxMenuItem.enabled(this.discardCurrentTrackRecordingMenuItem!!)),
                RxMenuItem.clicks(this.discardCurrentTrackRecordingMenuItem!!).subscribe({
                    this.viewModel!!.discardCurrentTrackRecording()
                })
        )
    }

    override fun onPause() {
        super.onPause()

        this.viewModel!!.saveCurrentTrackRecording()

        this.subscriptions.clear()
        this.foregroundTrackRecorderMapLocationSubscription?.dispose()
    }

    override fun onDestroy() {
        super.onDestroy()

        this.viewModel!!.pauseRecording()

        this.viewModel!!.saveCurrentTrackRecording()

        this.viewModel!!.dispose()
    }

    private fun initializeGoogleMap() {
        val mapFragment = this.fragmentManager.findFragmentById(R.id.trackrecorderactivity_googlemap_mapfragment) as MapFragment

        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        // Zoom to awesome Kackstadt :)
        this.trackRecorderMap = TrackRecorderMap.fromGoogleMap(googleMap, LatLng(50.8357, 12.92922), 13f)
    }
}