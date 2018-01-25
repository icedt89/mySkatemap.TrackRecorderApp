package com.janhafner.myskatemap.apps.trackrecorder.location

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.janhafner.myskatemap.apps.trackrecorder.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.CurrentTrackRecordingStore
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.IDataStore
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.ObservableTimer
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.TrackDistanceCalculator
import com.janhafner.myskatemap.apps.trackrecorder.location.provider.FusedLocationProvider
import com.janhafner.myskatemap.apps.trackrecorder.location.provider.ILocationProvider
import com.janhafner.myskatemap.apps.trackrecorder.location.provider.LegacyLocationProvider
import com.janhafner.myskatemap.apps.trackrecorder.location.provider.TestLocationProvider
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import org.joda.time.Period

internal final class TrackRecorderService : Service(), ITrackRecorderService {
    private lateinit var locationProvider : ILocationProvider

    private lateinit var trackRecordingStore : IDataStore<TrackRecording>

    private lateinit var locationChangedBroadcasterReceiver : LocationAvailabilityChangedBroadcastReceiver

    private val durationTimer : ObservableTimer = ObservableTimer()

    private var trackRecorderServiceNotification : TrackRecorderServiceNotification? = null

    private val trackDistanceCalculator : TrackDistanceCalculator = TrackDistanceCalculator()

    private lateinit var locationAvailabilityChangedSubscription : Disposable

    private var sessionSubscriptions : CompositeDisposable? = null

    private var currentTrackRecording : TrackRecording? = null

    private val stateChangedSubject : BehaviorSubject<TrackRecorderServiceState> =  BehaviorSubject.createDefault<TrackRecorderServiceState>(TrackRecorderServiceState.Initializing)

    public override var currentSession : ITrackRecordingSession? = null
        private set

    private fun initializeLocationAvailabilityChangedBroadcastReceiver() {
        this.locationChangedBroadcasterReceiver = LocationAvailabilityChangedBroadcastReceiver(this)
        this.registerReceiver(this.locationChangedBroadcasterReceiver, android.content.IntentFilter("android.location.PROVIDERS_CHANGED"))
        this.locationAvailabilityChangedSubscription = this.locationChangedBroadcasterReceiver.locationAvailabilityChanged.subscribe{
            isLocationModeEnabled ->
                if(!isLocationModeEnabled) {
                    if(this.stateChangedSubject.value == TrackRecorderServiceState.Running) {
                        this.pauseTracking()

                        this.changeState(TrackRecorderServiceState.LocationServicesUnavailable)
                    }
                } else {
                    if(this.stateChangedSubject.value == TrackRecorderServiceState.LocationServicesUnavailable) {
                        this.resumeTracking()

                        this.changeState(TrackRecorderServiceState.Running)
                    }
                }

                Log.v("TrackRecord", isLocationModeEnabled.toString())
        }
    }

    public fun resumeTracking() {
        if(this.stateChangedSubject.value != TrackRecorderServiceState.Ready
                && this.stateChangedSubject.value != TrackRecorderServiceState.Paused) {
            throw IllegalStateException()
        }

        this.changeState(TrackRecorderServiceState.Running)

        this.durationTimer.start()

        this.locationProvider.startLocationUpdates()
    }

    public fun pauseTracking() {
        if(this.stateChangedSubject.value != TrackRecorderServiceState.Running) {
            throw IllegalStateException()
        }

        this.durationTimer.stop()

        this.locationProvider.stopLocationUpdates()

        this.saveTracking()

        this.changeState(TrackRecorderServiceState.Paused)
    }

    public override fun discardTracking() {
        if(this.stateChangedSubject.value == TrackRecorderServiceState.Running) {
            throw IllegalStateException()
        }

        if(this.durationTimer.isRunning) {
            this.durationTimer.stop()
        }

        this.durationTimer.reset()

        if(this.locationProvider.isActive) {
            this.locationProvider.stopLocationUpdates()
        }

        this.trackDistanceCalculator.clear()

        this.trackRecordingStore.delete()

        this.currentTrackRecording = null

        this.currentSession!!.terminate()
        this.currentSession = null
    }

    public override fun finishTracking() : TrackRecording {
        // TODO: Check other states to provide meaningful exception messages
        if(this.stateChangedSubject.value == TrackRecorderServiceState.Running) {
            throw IllegalStateException()
        }

        val finishedTrackRecording  = this.currentTrackRecording!!

        this.saveTracking()

        this.discardTracking()

        return finishedTrackRecording
    }

    public override fun createSession(name: String) : ITrackRecordingSession {
        if(this.currentSession != null) {
            throw IllegalStateException()
        }

        this.currentTrackRecording = TrackRecording(name)

        this.saveTracking()

        this.currentSession = TrackRecordingSession.createWithSharedObservableSources(
                this.trackDistanceCalculator.distanceCalculated,
                this.durationTimer.secondElapsed,
                this.locationProvider.locations.replay().autoConnect(),
                this.stateChangedSubject,
                this)

        this.subscribeToSession()

        this.changeState(TrackRecorderServiceState.Ready)

        return this.currentSession!!
    }

    public override fun createSession(trackRecording : TrackRecording) : ITrackRecordingSession {
        if(this.currentSession != null) {
            throw IllegalStateException()
        }
        
        if(trackRecording.isFinished) {
            throw IllegalStateException()
        }

        var locationsChanged = this.locationProvider.locations.replay().autoConnect()
        if(trackRecording.locations.any()) {
            val sortedLocations = trackRecording.locations.sortedBy { location -> location.sequenceNumber }

            this.locationProvider.overrideSequenceNumber(sortedLocations.last().sequenceNumber)
        }

        var recordingTimeChanged = this.durationTimer.secondElapsed
        if(trackRecording.recordingTime != Period.ZERO && trackRecording.recordingTime.seconds > 0) {
            val duration = trackRecording.recordingTime!!

            this.durationTimer.reset(duration.seconds)
        }

        this.currentTrackRecording = trackRecording

        this.currentSession = TrackRecordingSession.createWithSharedObservableSources(
                this.trackDistanceCalculator.distanceCalculated,
                recordingTimeChanged,
                locationsChanged,
                this.stateChangedSubject,
                this)

        this.subscribeToSession()

        this.changeState(TrackRecorderServiceState.Paused)

        return this.currentSession!!
    }

    private fun subscribeToSession() {
        this.sessionSubscriptions = CompositeDisposable()

        this.sessionSubscriptions!!.addAll(
            this.currentSession!!.recordingTimeChanged.subscribe{
                currentRecordingTime ->
                    this.currentTrackRecording!!.recordingTime = currentRecordingTime
            },
            this.currentSession!!.locationsChanged.subscribe ({
                location ->
                    this.trackDistanceCalculator.add(location)

                    this.currentTrackRecording!!.locations.add(location)

                    Log.i("TrackRecorderService", "Location ${location} received for persistance.")
            })
        )
    }

    private fun unsubscribeFromSession() {
        if(this.sessionSubscriptions != null) {
            this.sessionSubscriptions!!.dispose()
            this.sessionSubscriptions = null
        }
    }

    public fun saveTracking() {
        if(this.currentTrackRecording == null) {
            throw IllegalStateException()
        }

        this.trackRecordingStore.save(this.currentTrackRecording!!)
    }

    private fun changeState(newState : TrackRecorderServiceState) {
        this.stateChangedSubject.onNext(newState)

        if(this.trackRecorderServiceNotification == null) {
            this.trackRecorderServiceNotification = TrackRecorderServiceNotification.showNew(this, newState, null, null)
        } else {
            this.trackRecorderServiceNotification?.update(newState, this.currentTrackRecording?.recordingTime, this.trackDistanceCalculator.distance)
        }
    }

    public override fun onBind(intent: Intent?) : IBinder {
        return TrackRecorderServiceBinder(this)
    }

    public override fun onStartCommand(intent: Intent?, flags: Int, startId: Int) : Int {
        return START_STICKY
    }

    public override fun onCreate() {
        this.trackRecordingStore = CurrentTrackRecordingStore(this)
        this.locationProvider = this.createLocationProvider(true, false)

        this.initializeLocationAvailabilityChangedBroadcastReceiver()

        this.changeState(TrackRecorderServiceState.Initializing)
    }

    private fun createLocationProvider(useEmulatedLocationProvider : Boolean, useLegacyLocationProvider : Boolean) : ILocationProvider {
        if (useEmulatedLocationProvider) {
            val initialLocation : Location = Location(-1)

            initialLocation.bearing = 1.0f
            initialLocation.latitude = 50.8333
            initialLocation.longitude = 12.9167

            return TestLocationProvider(this, initialLocation, delay = 2500, interval = 500)
        } else {
            if(useLegacyLocationProvider) {
                return LegacyLocationProvider(this)
            }

            return FusedLocationProvider(this)
        }
    }

    public override fun onDestroy() {
        if(this.locationProvider.isActive) {
            this.locationProvider.stopLocationUpdates()
        }

        if(this.durationTimer.isRunning) {
            this.durationTimer.stop()
        }

        this.durationTimer.reset()

        this.unregisterReceiver(this.locationChangedBroadcasterReceiver)

        this.trackRecorderServiceNotification?.close()

        this.locationAvailabilityChangedSubscription.dispose()
    }
}