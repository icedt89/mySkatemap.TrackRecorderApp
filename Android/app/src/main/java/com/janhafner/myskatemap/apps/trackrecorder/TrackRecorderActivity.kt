package com.janhafner.myskatemap.apps.trackrecorder

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.design.widget.FloatingActionButton
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.Toast
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.CurrentTrackRecordingStore
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.IDataStore
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.ObservableSubscription
import com.janhafner.myskatemap.apps.trackrecorder.location.ITrackRecorderService
import com.janhafner.myskatemap.apps.trackrecorder.location.TrackRecorderService
import com.janhafner.myskatemap.apps.trackrecorder.location.TrackRecorderServiceBinder
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject
import java.util.*

internal final class TrackRecorderActivity : AppCompatActivity, OnMapReadyCallback, Observer {
    private final var trackRecorderMap: ITrackRecorderMap?;

    private final var trackRecorderRecordButton: FloatingActionButton?;

    private final var trackRecorderStopButton: FloatingActionButton?;

    private final var currentTrackRecordingStore: IDataStore<CurrentTrackRecording>?;

    private final var currentTrackRecording: CurrentTrackRecording?;

    private final var trackRecorderServiceSubscription: ObservableSubscription?;

    private final val trackRecorderServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            this@TrackRecorderActivity.trackRecorderService = (service as TrackRecorderServiceBinder).service;

            this@TrackRecorderActivity.trackRecorderServiceSubscription = this@TrackRecorderActivity.trackRecorderService!!.addIsActiveObserver(this@TrackRecorderActivity);
        }

        override fun onServiceDisconnected(name: ComponentName) {
            this@TrackRecorderActivity.trackRecorderService = null;

            this@TrackRecorderActivity!!.trackRecorderServiceSubscription!!.remove();
        }
    }

    private var trackRecorderService: ITrackRecorderService? = null;

    public constructor() : super(){
        this.trackRecorderMap = null;
        this.trackRecorderRecordButton = null;
        this.trackRecorderStopButton = null;
        this.currentTrackRecordingStore = null;
        this.currentTrackRecording = null;
        this.trackRecorderServiceSubscription = null;
    }

    override final fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.activity_track_recorder);

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
                .check();

        this.bindToTrackRecorderService();

        val trackRecorderToolbar = this.findViewById<Toolbar>(R.id.trackRecorderToolbar);
        this.setSupportActionBar(trackRecorderToolbar);

        this.trackRecorderRecordButton = this.findViewById<FloatingActionButton>(R.id.trackRecorderRecordButton);
        this.trackRecorderRecordButton!!.setOnClickListener({
            this.trackRecorderService!!.startLocationTracking()
        });

        this.trackRecorderStopButton = this.findViewById<FloatingActionButton>(R.id.trackRecorderStopButton);
        this.trackRecorderStopButton!!.setOnClickListener({
            this.trackRecorderService!!.stopLocationTracking()
        });

        val recordedTrackSwipeRefresher = this.findViewById<SwipeRefreshLayout>(R.id.recordedTrackSwipeRefresher);
        recordedTrackSwipeRefresher.setOnRefreshListener ({
            this.refreshTrackPathOnMap();

            recordedTrackSwipeRefresher.isRefreshing = false;
        });

        this.initializeGoogleMap();

        this.currentTrackRecordingStore = CurrentTrackRecordingStore(this);
    }

    override final fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menuInflater.inflate(R.menu.track_recorder_activity_toolbar_menu, menu);

        menu.findItem(R.id.trackrecorderactivity_toolbar_refresh).setOnMenuItemClickListener({
            this.refreshTrackPathOnMap();

            true;
        });

        menu.findItem(R.id.trackrecorderactivity_toolbar_attachments).setOnMenuItemClickListener { true };

        menu.findItem(R.id.trackrecorderactivity_toolbar_finish).setOnMenuItemClickListener ({


            true;
        });

        menu.findItem(R.id.trackrecorderactivity_toolbar_discard).setOnMenuItemClickListener ({
            this.discardCurrentTrackRecording();

            true;
        });

        return true;
    }

    private final fun refreshTrackPathOnMap() {
        this.trackRecorderMap!!.setRecordedTrack(this.trackRecorderService!!.locations);

        this.saveCurrentState();
    }

    private final fun saveCurrentState() {
        this.currentTrackRecording!!.locations.clear();
        this.currentTrackRecording!!.locations.addAll(this.trackRecorderService!!.locations);

        this.currentTrackRecordingStore!!.save(this.currentTrackRecording!!);
    }

    private final fun discardCurrentTrackRecording() {
        this.trackRecorderService!!.stopLocationTracking();

        this.currentTrackRecording = null;

        this.currentTrackRecordingStore?.delete();
    }

    override final fun onPause() {
        this.saveCurrentState();

        super.onPause();
    }

    override final fun onDestroy() {
        this.trackRecorderService!!.stopLocationTracking();

        this.saveCurrentState();

        this.unbindFromTrackRecorderServic();

        super.onDestroy();
    }

    private final fun unbindFromTrackRecorderServic() {
        this.stopService(Intent(this, TrackRecorderService::class.java));

        this.unbindService(this.trackRecorderServiceConnection);
    }

    private final fun bindToTrackRecorderService() {
        this.startService(Intent(this, TrackRecorderService::class.java));

        this.bindService(Intent(this, TrackRecorderService::class.java), this.trackRecorderServiceConnection, BIND_AUTO_CREATE);
    }

    private final fun initializeGoogleMap() {
        val mapFragment = this.fragmentManager.findFragmentById(R.id.trackRecorderMapFragment) as MapFragment;
        mapFragment.getMapAsync(this);
    }

    override final fun onMapReady(googleMap: GoogleMap) {
        if (googleMap == null) {
            throw IllegalArgumentException("googleMap");
        }

        this.trackRecorderMap = TrackRecorderMap.fromGoogleMap(googleMap, LatLng(50.8357, 12.92922), 13f);
    }

    public override final fun update(o: Observable?, arg: Any?) {
        if(arg is Boolean) {
            if(arg) {
                if(this.currentTrackRecording == null){
                    this.currentTrackRecording = CurrentTrackRecording();
                }

                this@TrackRecorderActivity.trackRecorderStopButton!!.visibility = View.VISIBLE
                this@TrackRecorderActivity.trackRecorderRecordButton!!.visibility = View.GONE
            } else {
                this@TrackRecorderActivity.trackRecorderStopButton!!.visibility = View.GONE
                this@TrackRecorderActivity.trackRecorderRecordButton!!.visibility = View.VISIBLE
            }
        }
    }
}