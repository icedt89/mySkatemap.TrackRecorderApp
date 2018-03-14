package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.janhafner.myskatemap.apps.trackrecorder.data.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.ObservableTimer
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance.TrackDistanceCalculator
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.CurrentTrackRecordingStore
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.IFileBasedDataStore
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.settings.AppSettings
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.isLocationServicesEnabled
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
import javax.inject.Inject

internal final class TrackRecorderService: Service(), ITrackRecorderService {
    private val logTag: String = this.javaClass.name

    @Deprecated("Inject using Dagger")
    private lateinit var locationProvider: ILocationProvider

    @Deprecated("Inject using Dagger")
    private lateinit var trackRecordingStoreFileBased: IFileBasedDataStore<TrackRecording>

    @Deprecated("Inject using Dagger")
    private lateinit var locationChangedBroadcasterReceiver: LocationAvailabilityChangedBroadcastReceiver

    @Deprecated("Inject using Dagger")
    private val durationTimer: ObservableTimer = ObservableTimer()

    private lateinit var trackRecorderServiceNotification: TrackRecorderServiceNotification

    @Deprecated("Inject using Dagger")
    @Inject
    public lateinit var trackDistanceCalculator: TrackDistanceCalculator

    private lateinit var locationServicesAvailabilityChangedSubscription: Disposable

    private val sessionSubscriptions: CompositeDisposable = CompositeDisposable()

    private val stateChangedSubject: BehaviorSubject<TrackRecorderServiceState> = BehaviorSubject.createDefault<TrackRecorderServiceState>(TrackRecorderServiceState.Initializing)

    public var currentTrackRecording: TrackRecording? = null

    @Deprecated("Resolve using Dagger! React to change of flash color and vibrate: set notification properties!")
    private val appSettings: IAppSettings = AppSettings()

    public override var currentSession: ITrackRecordingSession? = null
        private set

    public override lateinit var locationServicesAvailability: Observable<Boolean>
        private set

    private fun initializeLocationAvailabilityChangedBroadcastReceiver() {
        this.locationChangedBroadcasterReceiver = LocationAvailabilityChangedBroadcastReceiver(this)
        this.registerReceiver(this.locationChangedBroadcasterReceiver, android.content.IntentFilter(LocationAvailabilityChangedBroadcastReceiver.PROVIDERS_CHANGED))
        this.locationServicesAvailabilityChangedSubscription = this.locationChangedBroadcasterReceiver.locationAvailabilityChanged.subscribe{
            if (!it) {
                if (this.stateChangedSubject.value != TrackRecorderServiceState.Initializing) {
                    this.pauseTrackingAndSetState(com.janhafner.myskatemap.apps.trackrecorder.location.TrackRecorderServiceState.LocationServicesUnavailable)
                }
            } else {
                if (this.stateChangedSubject.value == TrackRecorderServiceState.LocationServicesUnavailable) {
                    this.resumeTracking()
                }
            }
        }
        
        this.locationServicesAvailability = this.locationChangedBroadcasterReceiver.locationAvailabilityChanged
    }

    public fun resumeTracking() {
        val state = this.stateChangedSubject.value
        if (state != TrackRecorderServiceState.Ready
                && state != TrackRecorderServiceState.Paused
                && state != TrackRecorderServiceState.LocationServicesUnavailable) {
            throw IllegalStateException()
        }

        this.locationProvider.startLocationUpdates()
        this.durationTimer.start()

        this.currentTrackRecording!!.resumed()

        this.changeState(TrackRecorderServiceState.Running)
    }

    public fun pauseTracking() {
        this.pauseTrackingAndSetState(com.janhafner.myskatemap.apps.trackrecorder.location.TrackRecorderServiceState.Paused)
    }

    private fun pauseTrackingAndSetState(state: TrackRecorderServiceState) {
        if (this.stateChangedSubject.value == TrackRecorderServiceState.Initializing
            || this.stateChangedSubject.value == TrackRecorderServiceState.Ready) {
            throw IllegalStateException()
        }

        if(this.stateChangedSubject.value == TrackRecorderServiceState.Running) {
            this.locationProvider.stopLocationUpdates()
            this.durationTimer.stop()

            this.currentTrackRecording!!.paused()

            this.saveTracking()
        }

        this.changeState(state)
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

        this.currentSession = null
    }

    public override fun useTrackRecording(trackRecording: TrackRecording): ITrackRecordingSession {
        if (this.currentSession != null) {
            throw IllegalStateException()
        }

        var locationsChanged = this.locationProvider.locations.replay().autoConnect()
        if (trackRecording.locations.any()) {
            val sortedLocations = trackRecording.locations.toSortedMap().values

            this.locationProvider.overrideSequenceNumber(sortedLocations.last().sequenceNumber)

            locationsChanged = locationsChanged.startWith(sortedLocations)
        }

        val recordingTimeChanged = this.durationTimer.secondElapsed
        if (trackRecording.recordingTime != Period.ZERO && trackRecording.recordingTime.seconds > 0) {
            this.durationTimer.reset(trackRecording.recordingTime.seconds)
        }

        this.createSessionCore(trackRecording, recordingTimeChanged, locationsChanged)

        if(this.isLocationServicesEnabled()) {
            this.changeState(TrackRecorderServiceState.Paused)
        } else {
            this.changeState(TrackRecorderServiceState.LocationServicesUnavailable)
        }

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

                    this.trackRecorderServiceNotification.durationOfRecording = currentRecordingTime
            },

            this.currentSession!!.locationsChanged
                    .buffer(1, TimeUnit.SECONDS)
                    .filter{
                        it.any()
                    }
                    .subscribe {
                        this.trackDistanceCalculator.addAll(it)

                        this.currentTrackRecording!!.locations.putAll(it.map{
                            Pair(it.sequenceNumber, it)
                        })
                    },

                this.stateChangedSubject.subscribe {
                    this.trackRecorderServiceNotification.state = it

                    if(it == TrackRecorderServiceState.Initializing) {
                        this.trackRecorderServiceNotification.close()
                    } else  {
                        this.trackRecorderServiceNotification.update()
                    }
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

    public override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intent != null) {
            when(intent.action) {
                TrackRecorderServiceNotification.ACTION_RESUME ->
                    this.resumeTracking()
                TrackRecorderServiceNotification.ACTION_PAUSE ->
                    this.pauseTracking()
                TrackRecorderServiceNotification.ACTION_TERMINATE -> {
                    this.trackRecorderServiceNotification.close()

                    this.stopSelf()
                }
            }
        }

        return START_NOT_STICKY
    }

    public override fun onCreate() {
        DaggerTrackRecorderServiceComponent.create().inject(this)

        this.trackRecordingStoreFileBased = CurrentTrackRecordingStore(this)
        this.locationProvider = this.createLocationProvider(TestLocationProvider::javaClass.name)

        this.trackRecorderServiceNotification = TrackRecorderServiceNotification(this)
        this.trackRecorderServiceNotification.flashColorOnLocationUnavailableState = this.appSettings.notificationFlashColorOnBackgroundStop
        this.trackRecorderServiceNotification.vibrateOnLocationUnavailableState = this.appSettings.vibrateOnBackgroundStop

        this.initializeLocationAvailabilityChangedBroadcastReceiver()

        this.appSettings.appSettingsChanged.subscribe {
            if(it.propertyName == "notificationFlashColorOnBackgroundStop" && it.oldValue != it.newValue) {
                this.trackRecorderServiceNotification.flashColorOnLocationUnavailableState = it.newValue as Int?
            } else if(it.propertyName == "vibrateOnBackgroundStop" && it.oldValue != it.newValue) {
                this.trackRecorderServiceNotification.vibrateOnLocationUnavailableState = it.newValue as Boolean
            }

            this.trackRecorderServiceNotification.update()
        }

        this.changeState(TrackRecorderServiceState.Initializing)
    }

    private fun createLocationProvider(locationProviderTypeName: String): ILocationProvider {
        if (locationProviderTypeName == TestLocationProvider::javaClass.name) {
            val initialLocation: Location = Location(-1)

            initialLocation.bearing = 1.0f
            initialLocation.latitude = 50.8333
            initialLocation.longitude = 12.9167

            return TestLocationProvider(this, initialLocation, interval = 500)
        }

        if (locationProviderTypeName == LegacyLocationProvider::javaClass.name) {
            return LegacyLocationProvider(this)
        }

        if(locationProviderTypeName != FusedLocationProvider::javaClass.name) {
            Log.wtf(this.logTag, "Not a valid LocationProvider implementation: \"${locationProviderTypeName}\"")
        }

        return FusedLocationProvider(this)
    }


    public override fun onBind(intent: Intent?): IBinder {
        this.trackRecorderServiceNotification.userInitiatedServiceTerminationAllowed = false
        this.trackRecorderServiceNotification.update()

        return TrackRecorderServiceBinder(this)
    }

    public override fun onUnbind(intent: Intent?): Boolean {
        this.trackRecorderServiceNotification.userInitiatedServiceTerminationAllowed = true
        this.trackRecorderServiceNotification.update()

        return super.onUnbind(intent)
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
        this.locationServicesAvailabilityChangedSubscription.dispose()
    }
}