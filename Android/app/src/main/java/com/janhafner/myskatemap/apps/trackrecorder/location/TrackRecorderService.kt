package com.janhafner.myskatemap.apps.trackrecorder.location

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.janhafner.myskatemap.apps.trackrecorder.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.CurrentTrackRecordingStore
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.IDataStore
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.ObservableTimer
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.ObservableTimerState
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import org.joda.time.Duration

internal final class TrackRecorderService : Service(), ITrackRecorderService {
    private lateinit var locationProvider : ILocationProvider

    private lateinit var trackRecordingStore : IDataStore<TrackRecording>

    private val durationTimer : ObservableTimer = ObservableTimer()

    private var recordingNotification : TrackRecorderServiceNotification? = null

    private lateinit var locationsSubscription : Disposable

    private lateinit var recordingDurationSubscription : Disposable

    private var currentTrackRecording : TrackRecording? = null

    public override lateinit var locations : Observable<Location>
        get
        private set

    private val stateChangedSubject : BehaviorSubject<TrackRecorderServiceState> =  BehaviorSubject.createDefault<TrackRecorderServiceState>(TrackRecorderServiceState.Initializing)
    public override val stateChanged : Observable<TrackRecorderServiceState>
        get() = this.stateChangedSubject

    public override val state : TrackRecorderServiceState
        get() = this.stateChangedSubject.value

    public override lateinit var recordingDuration : Observable<Duration>
        get
        private set

    private val trackLengthSubject : BehaviorSubject<Float> = BehaviorSubject.createDefault<Float>(0f)
    public override val trackLength : Observable<Float>
        get() = this.trackLengthSubject

    private fun createDurationObservable(duration : Duration) : Observable<Duration> {
        var result : Observable<Duration> = this.durationTimer.secondElapsed

        if(duration != Duration.ZERO && duration.standardSeconds > 0) {
            result = result.startWith(duration)
        }

        return result.share()
    }

    private fun initializeDurationObservable(duration : Duration) {
        this.recordingDuration = this.createDurationObservable(duration)

        // Subscribe with a non/or debug emitting subscriber to force the replay subject to work instantly as expected
        this.recordingDurationSubscription = this.recordingDuration.subscribe {
            duration ->
                this.currentTrackRecording?.duration = duration

                Log.v("TrackRecorderService", "Elapsed: ${duration}")
        }
    }

    private fun createLocationsObservable(emittingSource : Observable<Location>, locations : Iterable<Location>? = null) : Observable<Location> {
        // replay()         = remember all emitted locations
        // autoConnect()    = start emitting/remembering emitted locations if at least one subscriber is attached
        var result : Observable<Location> = emittingSource.replay().autoConnect()

        if(locations != null && locations.any()) {
        // startWith()      = emit the supplied restored locations to each new subscriber
            result = result.startWith(locations)
        }

        // share()          = seriously, i don`t have really understood if i need this, but it doesnt hurt to append it...
        return result.share()
    }


    private fun initializeLocationObservable(locations : Iterable<Location>?) {
        this.locations = this.createLocationsObservable(this.locationProvider.locations, locations)

        // Subscribe with a non/or debug emitting subscriber to force the replay subject to work instantly as expected
        this.locationsSubscription = this.locations.subscribe {
            location ->
                this.currentTrackRecording?.locations?.add(location)

                Log.v("TrackRecorderService", "${location} handled")
        }
    }

    public override fun resumeTracking() {
        if(this.state != TrackRecorderServiceState.Ready && this.state != TrackRecorderServiceState.Paused) {
            throw IllegalStateException()
        }

        this.changeState(com.janhafner.myskatemap.apps.trackrecorder.location.TrackRecorderServiceState.Running)

        this.durationTimer.start()

        this.locationProvider.startLocationUpdates()
    }

    public override fun pauseTracking() {
        if(this.state != TrackRecorderServiceState.Running) {
            return
        }

        this.durationTimer.stop()

        this.locationProvider.stopLocationUpdates()

        this.saveTracking()

        this.changeState(TrackRecorderServiceState.Paused)
    }

    public override fun discardTracking() {
        if(this.state == TrackRecorderServiceState.Running) {
            throw IllegalStateException()
        }

        this.trackRecordingStore.delete()

        this.currentTrackRecording = null
    }

    public override fun finishTracking() : TrackRecording {
        // TODO: Check other states to provide meaningful exception messages
        if(this.state == TrackRecorderServiceState.Running) {
            throw IllegalStateException()
        }

        val finishedTrackRecording  = this.currentTrackRecording!!

        this.saveTracking()

        this.discardTracking()

        return finishedTrackRecording
    }

    public override fun useTrackRecording(name: String) {
        this.currentTrackRecording = TrackRecording(name)

        this.saveTracking()

        this.changeState(TrackRecorderServiceState.Ready)
    }

    public override fun useTrackRecording(trackRecording : TrackRecording) {
        if(trackRecording.isFinished) {
            throw IllegalStateException()
        }

        var locations : Iterable<Location>? = null
        var duration : Duration = Duration.ZERO

        if(trackRecording.locations.any()) {
            locations = trackRecording.locations.sortedBy { location -> location.sequenceNumber }
            val lastLocation = locations.lastOrNull()
            var sequenceNumberOverride = -1
            if(lastLocation != null) {
                sequenceNumberOverride = lastLocation?.sequenceNumber
            }

            this.locationProvider.overrideSequenceNumber(sequenceNumberOverride)
        }

        if(trackRecording.duration != null) {
            duration = trackRecording.duration!!

            this.durationTimer.reset(duration.standardSeconds)
        }

        this.initializeDurationObservable(duration)

        this.initializeLocationObservable(locations)

        this.currentTrackRecording = trackRecording

        this.changeState(TrackRecorderServiceState.Paused)
    }

    public override fun saveTracking() {
        if(this.currentTrackRecording == null) {
            throw IllegalStateException()
        }

        this.trackRecordingStore.save(this.currentTrackRecording!!)
    }

    private fun changeState(newState : TrackRecorderServiceState) {
        this.stateChangedSubject.onNext(newState)

        if(newState == TrackRecorderServiceState.Initializing) {
            return
        }

        // TODO: Supply duration of recording and length of track
        if(this.recordingNotification == null) {
            this.recordingNotification = TrackRecorderServiceNotification.showNew(this, newState, null, null)
        } else {
            this.recordingNotification?.update(newState, this.currentTrackRecording?.duration, this.trackLengthSubject.value)
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
        this.locationProvider = this.createLocationProvider(true)
    }

    private fun createLocationProvider(useEmulatedLocationProvider : Boolean) : ILocationProvider {
        if (useEmulatedLocationProvider) {
            val initialLocation : Location = Location(-1)

            initialLocation.bearing = 1.0f
            initialLocation.latitude = 50.8333
            initialLocation.longitude = 12.9167

            return TestLocationProvider(initialLocation, delay = 2500, interval = 500)
        } else {
            return FusedLocationProvider(this)
        }
    }

    public override fun onDestroy() {
        if(this.locationProvider.isActive) {
            this.locationProvider.stopLocationUpdates()
        }

        if(this.durationTimer.state == ObservableTimerState.Running) {
            this.durationTimer.stop()
        }

        this.durationTimer.reset()

        this.recordingNotification?.close()
    }
}