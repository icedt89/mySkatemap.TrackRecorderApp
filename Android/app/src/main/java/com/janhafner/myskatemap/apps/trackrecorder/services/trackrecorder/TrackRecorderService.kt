package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import com.janhafner.myskatemap.apps.trackrecorder.ITrackService
import com.janhafner.myskatemap.apps.trackrecorder.getApplicationInjector
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance.ITrackDistanceUnitFormatterFactory
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance.TrackDistanceCalculator
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.data.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.isLocationServicesEnabled
import com.janhafner.myskatemap.apps.trackrecorder.location.*
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.notifications.TrackRecorderServiceNotification
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider.ILocationProvider
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import org.joda.time.Period
import java.util.concurrent.TimeUnit
import javax.inject.Inject

internal final class TrackRecorderService: Service(), ITrackRecorderService {
    @Inject
    public lateinit var locationProvider: ILocationProvider

    @Inject
    public lateinit var trackService: ITrackService

    @Inject
    public lateinit var locationChangedBroadcasterReceiver: LocationAvailabilityChangedBroadcastReceiver

    private val durationTimer: com.janhafner.myskatemap.apps.trackrecorder.infrastructure.ObservableTimer = com.janhafner.myskatemap.apps.trackrecorder.infrastructure.ObservableTimer()

    private lateinit var trackRecorderServiceNotification: TrackRecorderServiceNotification

    @Inject
    public lateinit var trackDistanceCalculator: TrackDistanceCalculator

    private val subscriptions: CompositeDisposable = CompositeDisposable()

    @Inject
    public lateinit var trackDistanceUnitFormatterFactory: ITrackDistanceUnitFormatterFactory

    private val sessionSubscriptions: CompositeDisposable = CompositeDisposable()

    private val stateChangedSubject: BehaviorSubject<TrackRecorderServiceState> = BehaviorSubject.createDefault<TrackRecorderServiceState>(TrackRecorderServiceState.Idle)

    public var currentTrackRecording: TrackRecording? = null

    @Inject
    public lateinit var appSettings: IAppSettings

    public override var currentSession: ITrackRecordingSession? = null
        private set

    public override lateinit var locationServicesAvailability: Observable<Boolean>
        private set

    private fun initializeLocationAvailabilityChangedBroadcastReceiver() {
        this.registerReceiver(this.locationChangedBroadcasterReceiver, android.content.IntentFilter(LocationAvailabilityChangedBroadcastReceiver.PROVIDERS_CHANGED))

        this.subscriptions.add(this.locationChangedBroadcasterReceiver.locationAvailabilityChanged.subscribe{
            if (!it) {
                if (this.stateChangedSubject.value != TrackRecorderServiceState.Idle) {
                    this.pauseTrackingAndSetState(com.janhafner.myskatemap.apps.trackrecorder.location.TrackRecorderServiceState.LocationServicesUnavailable)
                }
            } else {
                if (this.stateChangedSubject.value == TrackRecorderServiceState.LocationServicesUnavailable) {
                    this.resumeTracking()
                }
            }
        })
        
        this.locationServicesAvailability = this.locationChangedBroadcasterReceiver.locationAvailabilityChanged
    }

    public fun resumeTracking() {
        val state = this.stateChangedSubject.value
        if (state != TrackRecorderServiceState.Paused
                && state != TrackRecorderServiceState.LocationServicesUnavailable) {
            throw IllegalStateException()
        }

        this.locationProvider.startLocationUpdates()
        this.durationTimer.start()

        this.currentTrackRecording!!.resume()

        this.changeState(TrackRecorderServiceState.Running)
    }

    public fun pauseTracking() {
        this.pauseTrackingAndSetState(com.janhafner.myskatemap.apps.trackrecorder.location.TrackRecorderServiceState.Paused)
    }

    private fun pauseTrackingAndSetState(state: TrackRecorderServiceState) {
        if (this.stateChangedSubject.value == TrackRecorderServiceState.Idle) {
            throw IllegalStateException()
        }

        if(this.stateChangedSubject.value == TrackRecorderServiceState.Running) {
            this.locationProvider.stopLocationUpdates()
            this.durationTimer.stop()

            this.currentTrackRecording!!.pause()

            Handler().postDelayed({
                // Hack: Because processing of locationsReceived is buffered every second,
                // it can happen that by the time the tracking is saved the list of locationsReceived is still being updated.
                // To prevent exceptions, delay saving 1 second and hope updates are done; until I find a better solution!
                this.saveTracking()
            }, 1000)
        }

        this.changeState(state)
    }

    public fun discardTracking() {
        if (this.stateChangedSubject.value == TrackRecorderServiceState.Running
            || this.stateChangedSubject.value == TrackRecorderServiceState.Idle) {
            throw IllegalStateException()
        }

        this.durationTimer.reset()
        this.locationProvider.resetSequenceNumber()

        this.trackDistanceCalculator.clear()

        this.trackService.deleteCurrentTrackRecording()
        this.currentTrackRecording = null

        this.changeState(TrackRecorderServiceState.Idle)

        this.closeCurrentSession()
    }

    public fun finishTracking(): TrackRecording {
        if (this.stateChangedSubject.value == TrackRecorderServiceState.Running
            || this.stateChangedSubject.value == TrackRecorderServiceState.Idle) {
            throw IllegalStateException()
        }

        val finishedTrackRecording = this.currentTrackRecording!!

        finishedTrackRecording.finish()
        this.saveTracking()

        this.durationTimer.reset()
        this.locationProvider.resetSequenceNumber()

        this.trackDistanceCalculator.clear()

        this.trackService.deleteCurrentTrackRecording()
        this.currentTrackRecording = null

        this.changeState(TrackRecorderServiceState.Idle)

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

        var locationsChanged = this.locationProvider.locationsReceived.replay().autoConnect()
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
                        this.currentTrackRecording!!.locations.putAll(it.map{
                            Pair(it.sequenceNumber, it)
                        })
                    },
            this.currentSession!!.locationsChanged
                    .buffer(1, TimeUnit.SECONDS)
                    .filter{
                        it.any()
                    }
                    .subscribe {
                        this.trackDistanceCalculator.addAll(it)
                    },

            this.stateChangedSubject.subscribe {
                this.trackRecorderServiceNotification.state = it

                if(it == TrackRecorderServiceState.Idle) {
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

        if(this.trackService.hasCurrentTrackRecording()) {
            this.trackService.saveCurrentTrackRecording()
        } else {
            this.trackService.saveAsCurrentTrackRecording(this.currentTrackRecording!!)
        }
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
        this.getApplicationInjector().inject(this)

        this.trackRecorderServiceNotification = TrackRecorderServiceNotification(this, this.trackDistanceUnitFormatterFactory.createTrackDistanceUnitFormatter())
        this.trackRecorderServiceNotification.flashColorOnLocationUnavailableState = this.appSettings.notificationFlashColorOnBackgroundStop
        this.trackRecorderServiceNotification.vibrateOnLocationUnavailableState = this.appSettings.vibrateOnBackgroundStop

        this.initializeLocationAvailabilityChangedBroadcastReceiver()

        this.subscriptions.add(this.appSettings.appSettingsChanged.subscribe {
            if(it.propertyName == "notificationFlashColorOnBackgroundStop" && it.hasChanged) {
                this.trackRecorderServiceNotification.flashColorOnLocationUnavailableState = it.newValue as Int?

                this.trackRecorderServiceNotification.update()
            } else if(it.propertyName == "vibrateOnBackgroundStop" && it.hasChanged) {
                this.trackRecorderServiceNotification.vibrateOnLocationUnavailableState = it.newValue as Boolean

                this.trackRecorderServiceNotification.update()
            }

            if(it.propertyName == "trackDistanceUnitFormatterTypeName" && it.hasChanged) {
                this.trackRecorderServiceNotification.trackDistanceUnitFormatter = this.trackDistanceUnitFormatterFactory.createTrackDistanceUnitFormatter()

                this.trackRecorderServiceNotification.update()
            }
        })

        this.changeState(TrackRecorderServiceState.Idle)
    }

    public override fun onBind(intent: Intent?): IBinder {
        this.trackRecorderServiceNotification.isBound = false
        this.trackRecorderServiceNotification.update()

        return TrackRecorderServiceBinder(this)
    }

    public override fun onUnbind(intent: Intent?): Boolean {
        this.trackRecorderServiceNotification.isBound = true
        this.trackRecorderServiceNotification.update()

        super.onUnbind(intent)

        return true
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
        this.subscriptions.clear()
    }
}