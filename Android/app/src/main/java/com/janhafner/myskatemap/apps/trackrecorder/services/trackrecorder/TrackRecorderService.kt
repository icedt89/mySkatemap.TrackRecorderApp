package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import com.janhafner.myskatemap.apps.trackrecorder.services.ITrackService
import com.janhafner.myskatemap.apps.trackrecorder.getApplicationInjector
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.Location
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.calories.BurnedEnergyCalculator
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.calories.Sex
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance.ITrackDistanceUnitFormatterFactory
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance.TrackDistanceCalculator
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.data.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.live.ILiveLocationTrackingService
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.live.ILiveTrackingSession
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.statistics.TrackRecordingStatistic
import com.janhafner.myskatemap.apps.trackrecorder.isLocationServicesEnabled
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.notifications.TrackRecorderServiceNotification
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider.ILocationProvider
import com.janhafner.myskatemap.apps.trackrecorder.toSimpleLocation
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
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

    public var currentTrackRecordingStatistic: TrackRecordingStatistic? = null

    public var burnedEnergyCalculator: BurnedEnergyCalculator? = null

    @Inject
    public lateinit var appSettings: IAppSettings

    @Inject
    public lateinit var liveLocationTrackingService: ILiveLocationTrackingService

    private var liveTrackingSession: ILiveTrackingSession? = null

    private var liveTrackingSessionSubscription: Disposable? = null

    public override var currentSession: ITrackRecordingSession? = null
        private set

    private fun initializeLocationAvailabilityChangedBroadcastReceiver() {
        this.registerReceiver(this.locationChangedBroadcasterReceiver, android.content.IntentFilter(LocationAvailabilityChangedBroadcastReceiver.PROVIDERS_CHANGED))

        this.subscriptions.add(this.locationChangedBroadcasterReceiver.locationAvailabilityChanged.subscribe{
            if (!it) {
                if (this.stateChangedSubject.value != TrackRecorderServiceState.Idle) {
                    this.pauseTrackingAndSetState(TrackRecorderServiceState.LocationServicesUnavailable)
                }
            } else {
                if (this.stateChangedSubject.value == TrackRecorderServiceState.LocationServicesUnavailable) {
                    this.resumeTracking()
                }
            }
        })
    }

    public fun resumeTracking() {
        val state = this.stateChangedSubject.value
        if (state != TrackRecorderServiceState.Paused
                && state != TrackRecorderServiceState.LocationServicesUnavailable) {
            throw IllegalStateException("Tracking must be paused first!")
        }

        this.locationProvider.startLocationUpdates()
        this.durationTimer.start()

        this.currentTrackRecording!!.resume()

        this.changeState(TrackRecorderServiceState.Running)
    }

    public fun pauseTracking() {
        this.pauseTrackingAndSetState(TrackRecorderServiceState.Paused)
    }

    private fun pauseTrackingAndSetState(state: TrackRecorderServiceState) {
        if (this.stateChangedSubject.value == TrackRecorderServiceState.Idle) {
            throw IllegalStateException("Tracking must be started first!")
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
            throw IllegalStateException("Tracking must be paused first!")
        }

        this.durationTimer.reset()
        this.locationProvider.resetSequenceNumber()

        this.trackDistanceCalculator.clear()

        this.trackService.deleteTrackRecording(this.currentTrackRecording!!.id.toString())
        // TODO
        this.currentTrackRecording = null
        this.currentTrackRecordingStatistic = null
        this.burnedEnergyCalculator = null

        this.changeState(TrackRecorderServiceState.Idle)

        this.closeCurrentSession()
    }

    public fun finishTracking(): TrackRecording {
        if (this.stateChangedSubject.value == TrackRecorderServiceState.Running
            || this.stateChangedSubject.value == TrackRecorderServiceState.Idle) {
            throw IllegalStateException("Tracking must be paused first!")
        }

        val finishedTrackRecording = this.currentTrackRecording!!

        finishedTrackRecording.finish()
        this.saveTracking()

        this.durationTimer.reset()
        this.locationProvider.resetSequenceNumber()

        this.trackDistanceCalculator.clear()

        // TODO
        this.currentTrackRecording = null
        this.currentTrackRecordingStatistic = null
        this.burnedEnergyCalculator = null

        this.changeState(TrackRecorderServiceState.Idle)

        this.closeCurrentSession()

        return finishedTrackRecording
    }

    private fun closeCurrentSession() {
        this.sessionSubscriptions.clear()
        this.deactiveLiveTracking()

        this.currentSession = null

        this.appSettings.currentTrackRecordingId = null
    }

    public override fun useTrackRecording(trackRecording: TrackRecording): ITrackRecordingSession {
        if (this.currentSession != null) {
            throw IllegalStateException("Tracking already in progress!")
        }

        this.appSettings.currentTrackRecordingId = trackRecording.id

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
        this.currentTrackRecordingStatistic = TrackRecordingStatistic()
        // TODO
        this.burnedEnergyCalculator  = BurnedEnergyCalculator(65.5f, 171, 28, Sex.Male, 7.5f)

        this.currentSession = TrackRecordingSession(
                this.trackDistanceCalculator.distanceCalculated,
                recordingTimeChanged,
                locationsChanged,
                this.stateChangedSubject,
                this)

        this.subscribeToSession()
    }

    private fun subscribeToSession() {
        this.tryActivateLiveTracking(this.appSettings.allowLiveTracking)

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
                        this.currentTrackRecordingStatistic!!.addAll(it)
                    },

            this.currentSession!!.recordingTimeChanged
                    // TODO: .sample(5, TimeUnit.MINUTES)
                    .sample(1, TimeUnit.SECONDS)
                    .subscribe {
                        this.burnedEnergyCalculator!!.calculate(it.seconds)
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
                    .buffer(5, TimeUnit.SECONDS)
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
            throw IllegalStateException("No tracking in progress!")
        }

        this.trackService.saveTrackRecording(this.currentTrackRecording!!)
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
        this.trackRecorderServiceNotification.vibrateOnLocationUnavailableState = this.appSettings.vibrateOnBackgroundStop

        this.initializeLocationAvailabilityChangedBroadcastReceiver()

        this.subscriptions.add(this.appSettings.appSettingsChanged.subscribe {
            if(it.hasChanged && it.propertyName == "vibrateOnBackgroundStop") {
                this.trackRecorderServiceNotification.vibrateOnLocationUnavailableState = it.newValue as Boolean

                this.trackRecorderServiceNotification.update()
            }

            if(it.hasChanged && it.propertyName == "trackDistanceUnitFormatterTypeName") {
                this.trackRecorderServiceNotification.trackDistanceUnitFormatter = this.trackDistanceUnitFormatterFactory.createTrackDistanceUnitFormatter()

                this.trackRecorderServiceNotification.update()
            }

            if(it.hasChanged && it.propertyName == "allowLiveTracking") {
                val value = it.newValue as Boolean

                if(!value && this.liveTrackingSession != null) {
                    this.liveTrackingSession!!.endSession()

                    this.liveTrackingSession = null
                    this.liveTrackingSessionSubscription?.dispose()
                } else {
                    this.tryActivateLiveTracking(value)
                }
            }
        })

        this.changeState(TrackRecorderServiceState.Idle)
    }

    private fun tryActivateLiveTracking(allowLiveTracking: Boolean): Boolean {
        if(allowLiveTracking && this.liveTrackingSession == null && this.currentSession != null) {
            this.liveTrackingSession = this.liveLocationTrackingService.createSession()

            this.liveTrackingSessionSubscription = this.currentSession!!.locationsChanged
                    .buffer(5, TimeUnit.SECONDS)
                    .filter{
                        it.any()
                    }
                    .map {
                        it.map {
                            it.toSimpleLocation()
                        }
                    }
                    .subscribe {
                        this.liveTrackingSession!!.sendLocations(it)
                    }

            return true
        }

        return false
    }

    private fun deactiveLiveTracking() {
        this.liveTrackingSessionSubscription?.dispose()
    }

    public override fun onBind(intent: Intent?): IBinder {
        this.serviceBound()

        return TrackRecorderServiceBinder(this)
    }

    public override fun onRebind(intent: Intent?) {
        this.serviceBound()
    }

    private fun serviceBound() {
        this.trackRecorderServiceNotification.isBound = true
        this.trackRecorderServiceNotification.update()
    }

    public override fun onUnbind(intent: Intent?): Boolean {
        this.serviceUnbind()

        return true
    }

    private fun serviceUnbind() {
        this.trackRecorderServiceNotification.isBound = false
        this.trackRecorderServiceNotification.update()
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