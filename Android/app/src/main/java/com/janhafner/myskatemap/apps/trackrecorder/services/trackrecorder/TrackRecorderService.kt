package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.janhafner.myskatemap.apps.trackrecorder.data.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.ObservableTimer
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance.TrackDistanceCalculator
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.CurrentTrackRecordingStore
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.IFileBasedDataStore
import com.janhafner.myskatemap.apps.trackrecorder.location.*
import com.janhafner.myskatemap.apps.trackrecorder.location.provider.FusedLocationProvider
import com.janhafner.myskatemap.apps.trackrecorder.location.provider.ILocationProvider
import com.janhafner.myskatemap.apps.trackrecorder.location.provider.LegacyLocationProvider
import com.janhafner.myskatemap.apps.trackrecorder.location.provider.TestLocationProvider
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.notifications.TrackRecorderServiceNotification
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import org.joda.time.Period
import java.util.concurrent.TimeUnit

internal final class TrackRecorderService: Service(), ITrackRecorderService {
    private lateinit var locationProvider: ILocationProvider

    private lateinit var trackRecordingStoreFileBased: IFileBasedDataStore<TrackRecording>

    private lateinit var locationChangedBroadcasterReceiver: LocationAvailabilityChangedBroadcastReceiver

    private val durationTimer: ObservableTimer = ObservableTimer()

    private lateinit var trackRecorderServiceNotification: TrackRecorderServiceNotification

    private val trackDistanceCalculator: TrackDistanceCalculator = TrackDistanceCalculator()

    private lateinit var locationAvailabilityChangedSubscription: Disposable

    private val sessionSubscriptions: CompositeDisposable = CompositeDisposable()

    private val stateChangedSubject: BehaviorSubject<TrackRecorderServiceState> =  BehaviorSubject.createDefault<TrackRecorderServiceState>(TrackRecorderServiceState.Initializing)

    public var currentTrackRecording: TrackRecording? = null

    public override var currentSession: ITrackRecordingSession? = null
        private set

    private fun initializeLocationAvailabilityChangedBroadcastReceiver() {
        this.locationChangedBroadcasterReceiver = LocationAvailabilityChangedBroadcastReceiver(this)
        this.registerReceiver(this.locationChangedBroadcasterReceiver, android.content.IntentFilter())
        this.locationAvailabilityChangedSubscription = this.locationChangedBroadcasterReceiver.locationAvailabilityChanged.subscribe{
            if (!it) {
                if (this.stateChangedSubject.value == TrackRecorderServiceState.Running) {
                    this.pauseTracking()

                    this.changeState(TrackRecorderServiceState.LocationServicesUnavailable)
                }
            } else {
                if (this.stateChangedSubject.value == TrackRecorderServiceState.LocationServicesUnavailable) {
                    this.resumeTracking()

                    this.changeState(TrackRecorderServiceState.Running)
                }
            }
        }
    }

    public fun resumeTracking() {
        if (this.stateChangedSubject.value != TrackRecorderServiceState.Ready
                && this.stateChangedSubject.value != TrackRecorderServiceState.Paused) {
            throw IllegalStateException()
        }

        this.changeState(TrackRecorderServiceState.Running)

        this.durationTimer.start()
        this.locationProvider.startLocationUpdates()

        this.currentTrackRecording!!.resumed()
    }

    public fun pauseTracking() {
        if (this.stateChangedSubject.value != TrackRecorderServiceState.Running
            || this.stateChangedSubject.value == TrackRecorderServiceState.Initializing) {
            throw IllegalStateException()
        }

        this.durationTimer.stop()
        this.locationProvider.stopLocationUpdates()

        this.currentTrackRecording!!.paused()

        this.saveTracking()

        this.changeState(TrackRecorderServiceState.Paused)
    }

    public fun discardTracking() {
        if (this.stateChangedSubject.value == TrackRecorderServiceState.Running
            || this.stateChangedSubject.value == TrackRecorderServiceState.Initializing) {
            throw IllegalStateException()
        }

        this.durationTimer.reset()
        this.locationProvider.resetSequenceNumber()

        this.trackDistanceCalculator.clear()

        this.trackRecordingStoreFileBased.delete()
        this.currentTrackRecording = null

        this.changeState(TrackRecorderServiceState.Initializing)

        this.closeCurrentSession()
    }

    public fun finishTracking(): TrackRecording {
        if (this.stateChangedSubject.value == TrackRecorderServiceState.Running
            || this.stateChangedSubject.value == TrackRecorderServiceState.Initializing) {
            throw IllegalStateException()
        }

        val finishedTrackRecording = this.currentTrackRecording!!

        finishedTrackRecording.finished()
        this.saveTracking()

        this.durationTimer.reset()
        this.locationProvider.resetSequenceNumber()

        this.trackDistanceCalculator.clear()

        this.trackRecordingStoreFileBased.delete()
        this.currentTrackRecording = null

        this.changeState(TrackRecorderServiceState.Initializing)

        this.closeCurrentSession()

        return finishedTrackRecording
    }

    private fun closeCurrentSession() {
        this.sessionSubscriptions.clear()

        // TODO: this.currentSession!!.terminate()
        this.currentSession = null
    }

    public override fun createNewSession(name: String): ITrackRecordingSession {
        if (this.currentSession != null) {
            throw IllegalStateException()
        }

        this.createSessionCore(TrackRecording.start(name), this.durationTimer.secondElapsed, this.locationProvider.locations.replay().autoConnect())

        this.saveTracking()

        this.changeState(TrackRecorderServiceState.Ready)

        return this.currentSession!!
    }

    public override fun resumeSession(trackRecording: TrackRecording): ITrackRecordingSession {
        if (this.currentSession != null) {
            throw IllegalStateException()
        }
        
        if (trackRecording.isFinished) {
            throw IllegalStateException()
        }

        var locationsChanged = this.locationProvider.locations.replay().autoConnect()
        if (trackRecording.locations.any()) {
            val sortedLocations = trackRecording.locations.toSortedMap().values

            this.locationProvider.overrideSequenceNumber(sortedLocations.last().sequenceNumber)

            locationsChanged = locationsChanged.startWith(sortedLocations)
        }

        var recordingTimeChanged = this.durationTimer.secondElapsed
        if (trackRecording.recordingTime != Period.ZERO && trackRecording.recordingTime.seconds > 0) {
            this.durationTimer.reset(trackRecording.recordingTime.seconds)
        }

        this.createSessionCore(trackRecording, recordingTimeChanged, locationsChanged)

        this.changeState(TrackRecorderServiceState.Paused)

        return this.currentSession!!
    }

    private fun createSessionCore(trackRecording: TrackRecording, recordingTimeChanged: Observable<Period>, locationsChanged: Observable<Location>) {
        this.currentTrackRecording = trackRecording

        this.currentSession = TrackRecordingSession(
                this.trackDistanceCalculator.distanceCalculated,
                recordingTimeChanged,
                locationsChanged,
                this.stateChangedSubject,
                this)

        this.subscribeToSession()
    }

    private fun subscribeToSession() {
        this.sessionSubscriptions.addAll(
            this.currentSession!!.recordingTimeChanged.subscribe {
                currentRecordingTime ->
                    this.currentTrackRecording!!.recordingTime = currentRecordingTime

                    if(currentRecordingTime.seconds % 5 == 0) {
                        this.trackRecorderServiceNotification.durationOfRecording = currentRecordingTime

                        this.trackRecorderServiceNotification.update()
                    }
            },

            this.currentSession!!.locationsChanged
                    .buffer(5, TimeUnit.SECONDS)
                    .subscribe {
                        if(it.any()){
                            this.trackDistanceCalculator.addAll(it)

                            this.currentTrackRecording!!.locations.putAll(it.map{
                                Pair(it.sequenceNumber, it)
                            })
                        }
                    },

                this.stateChangedSubject.subscribe {
                    this.trackRecorderServiceNotification.state = it

                    this.trackRecorderServiceNotification.update()
                },

            this.currentSession!!.trackDistanceChanged.subscribe{
                this.trackRecorderServiceNotification.trackDistance = it

                this.trackRecorderServiceNotification.update()
            }
        )
    }

    public fun saveTracking() {
        if (this.currentTrackRecording == null) {
            throw IllegalStateException()
        }

        this.trackRecordingStoreFileBased.save(this.currentTrackRecording!!)
    }

    private fun changeState(newState: TrackRecorderServiceState) {
        this.stateChangedSubject.onNext(newState)
    }

    public override fun onBind(intent: Intent?): IBinder {
        return TrackRecorderServiceBinder(this)
    }

    public override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intent != null) {
            when(intent.action) {
                TrackRecorderServiceNotification.ACTION_RESUME ->
                    this.resumeTracking()
                TrackRecorderServiceNotification.ACTION_PAUSE ->
                    this.pauseTracking()
                TrackRecorderServiceNotification.ACTION_TERMINATE ->
                    this.stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    public override fun onCreate() {
        this.applicationContext
        this.trackRecordingStoreFileBased = CurrentTrackRecordingStore(this)
        this.locationProvider = this.createLocationProvider(true, false)
        this.trackRecorderServiceNotification = TrackRecorderServiceNotification(this)

        this.initializeLocationAvailabilityChangedBroadcastReceiver()

        this.changeState(TrackRecorderServiceState.Initializing)
    }

    private fun createLocationProvider(useEmulatedLocationProvider: Boolean, useLegacyLocationProvider: Boolean): ILocationProvider {
        if (useEmulatedLocationProvider) {
            val initialLocation: Location = Location(-1)

            initialLocation.bearing = 1.0f
            initialLocation.latitude = 50.8333
            initialLocation.longitude = 12.9167

            return TestLocationProvider(this, initialLocation, interval = 500)
        } else {
            if (useLegacyLocationProvider) {
                return LegacyLocationProvider(this)
            }

            return FusedLocationProvider(this)
        }
    }

    public override fun onDestroy() {
        if (this.locationProvider.isActive) {
            this.locationProvider.stopLocationUpdates()
        }

        if (this.durationTimer.isRunning) {
            this.durationTimer.stop()
        }

        this.unregisterReceiver(this.locationChangedBroadcasterReceiver)

        this.closeCurrentSession()

        this.trackRecorderServiceNotification.close()
        this.locationAvailabilityChangedSubscription.dispose()
    } }