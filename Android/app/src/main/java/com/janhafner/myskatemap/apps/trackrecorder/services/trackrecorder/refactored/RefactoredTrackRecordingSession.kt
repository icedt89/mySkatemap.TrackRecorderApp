package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.refactored

import android.app.Service
import android.location.LocationManager
import android.os.Handler
import android.util.Log
import com.janhafner.myskatemap.apps.trackrecorder.IObservableTimer
import com.janhafner.myskatemap.apps.trackrecorder.Nothing
import com.janhafner.myskatemap.apps.trackrecorder.io.data.Location
import com.janhafner.myskatemap.apps.trackrecorder.io.data.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.services.ITrackService
import com.janhafner.myskatemap.apps.trackrecorder.services.calories.IBurnedEnergyCalculator
import com.janhafner.myskatemap.apps.trackrecorder.services.distance.ITrackDistanceCalculator
import com.janhafner.myskatemap.apps.trackrecorder.services.distance.ITrackDistanceUnitFormatterFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.live.ILiveLocationTrackingSession
import com.janhafner.myskatemap.apps.trackrecorder.services.stilldetection.ActivityRecognizerIntentService
import com.janhafner.myskatemap.apps.trackrecorder.services.stilldetection.StillDetectorBroadcastReceiver
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.ITrackRecordingSession
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.LocationAvailabilityChangedBroadcastReceiver
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceState
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider.ILocationProvider
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppConfig
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.statistics.ITrackRecordingStatistic
import com.janhafner.myskatemap.apps.trackrecorder.toSimpleLocation
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import org.joda.time.DateTime
import org.joda.time.Period
import java.util.concurrent.TimeUnit

// Thats a shitload of dependencies...
internal final class RefactoredTrackRecordingSession(private val appSettings: IAppSettings,
                                                     private val appConfig: IAppConfig,
                                                     private val trackService: ITrackService,
                                                     private val trackRecorderServiceNotification: RefactoredTrackRecorderServiceNotification,
                                                     private val trackDistanceCalculator: ITrackDistanceCalculator,
                                                     private val trackDistanceUnitFormatterFactory: ITrackDistanceUnitFormatterFactory,
                                                     private val trackRecording: TrackRecording,
                                                     private val statistic: ITrackRecordingStatistic,
                                                     private val burnedEnergyCalculator: IBurnedEnergyCalculator,
                                                     private val locationProvider: ILocationProvider,
                                                     private val durationTimer: IObservableTimer,
                                                     private val liveLocationTrackingSession: ILiveLocationTrackingSession,
                                                     private val service: Service,
                                                     private val stillDetectorBroadcastReceiver: StillDetectorBroadcastReceiver,
                                                     private val locationAvailabilityChangedBroadcastReceiver: LocationAvailabilityChangedBroadcastReceiver) : ITrackRecordingSession {
    private val subscriptions: CompositeDisposable = CompositeDisposable()

    public override lateinit var recordingTimeChanged: Observable<Period>
        private set

    public override lateinit var locationsChanged: Observable<Location>
        private set

    public override val trackDistanceChanged: Observable<Float>
        get() = this.trackDistanceCalculator.distanceCalculated

    private val sessionClosedSubject: Subject<ITrackRecordingSession> = PublishSubject.create()
    public override val sessionClosed: Observable<ITrackRecordingSession>
        get() = this.sessionClosedSubject

    private val stateChangedSubject: BehaviorSubject<TrackRecorderServiceState> = BehaviorSubject.createDefault(TrackRecorderServiceState.Idle)
    public override val stateChanged: Observable<TrackRecorderServiceState>
        get() = this.stateChangedSubject

    init {
        this.locationsChanged = this.locationProvider.locationsReceived.replay().autoConnect()
        if (trackRecording.locations.any()) {
            val sortedLocations = trackRecording.locations.toSortedMap().values

            this.locationProvider.overrideSequenceNumber(sortedLocations.last().sequenceNumber)

            this.locationsChanged = locationsChanged.startWith(sortedLocations)
        }

        this.recordingTimeChanged = this.durationTimer.secondElapsed
        if (trackRecording.recordingTime != Period.ZERO && trackRecording.recordingTime.seconds > 0) {
            this.durationTimer.set(trackRecording.recordingTime.seconds)
        }

        this.initializeSession()
    }

    private fun initializeSession() {
        this.subscriptions.addAll(
                this.appSettings.appSettingsChanged.subscribe {
                    if (it.hasChanged && it.propertyName == "trackDistanceUnitFormatterTypeName") {
                        this.trackRecorderServiceNotification.trackDistanceUnitFormatter = this.trackDistanceUnitFormatterFactory.createTrackDistanceUnitFormatter()

                        this.tryUpdateNotification()
                    }
                },

                this.recordingTimeChanged
                        .sample(this.appConfig.updateBurnedEnergySeconds.toLong(), TimeUnit.SECONDS)
                        .subscribe {
                            this.burnedEnergyCalculator.calculate(it.seconds)
                        },

                this.recordingTimeChanged.subscribe { currentRecordingTime ->
                    this.trackRecording.recordingTime = currentRecordingTime

                    this.trackRecorderServiceNotification.durationOfRecording = currentRecordingTime
                },

                this.locationsChanged
                        .buffer(this.appConfig.updateStatisticsSeconds.toLong(), TimeUnit.SECONDS)
                        .filter {
                            it.any()
                        }
                        .subscribe {
                            this.statistic.addAll(it)
                        },
                this.locationsChanged
                        .buffer(this.appConfig.updateTrackRecordingLocationsSeconds.toLong(), TimeUnit.SECONDS)
                        .filter {
                            it.any()
                        }
                        .subscribe {
                            this.trackRecording.locations.putAll(it.map {
                                Pair(it.sequenceNumber, it)
                            })
                        },
                this.locationsChanged
                        .buffer(this.appConfig.updateTrackDistanceSeconds.toLong(), TimeUnit.SECONDS)
                        .filter {
                            it.any()
                        }
                        .subscribe {
                            this.trackDistanceCalculator.addAll(it)
                        },
                this.locationsChanged
                        .buffer(this.appConfig.updateLiveLocationSession.toLong(), TimeUnit.SECONDS)
                        .filter{
                            it.any()
                        }
                        .map {
                            it.map {
                                it.toSimpleLocation()
                            }
                        }
                        .subscribe {
                            this.liveLocationTrackingSession.sendLocations(it)
                        },

                this.stateChanged.subscribe {
                    this.trackRecorderServiceNotification.state = it

                    if (it == TrackRecorderServiceState.Idle) {
                        this.service.stopForeground(true)
                    } else {
                        this.tryUpdateNotification()
                    }
                },

                this.trackDistanceChanged.subscribe {
                    this.trackRecorderServiceNotification.trackDistance = it

                    this.tryUpdateNotification()
                })

        this.initializeLocationAvailabilityChangedBroadcastReceiver()
        this.initializeStillDetectorBroadcastReceiver()
    }

    private fun initializeLocationAvailabilityChangedBroadcastReceiver() {
        this.service.registerReceiver(this.locationAvailabilityChangedBroadcastReceiver, android.content.IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION))

        this.subscriptions.add(this.locationAvailabilityChangedBroadcastReceiver.locationAvailabilityChanged.subscribe{
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

    private fun initializeStillDetectorBroadcastReceiver() {
        this.service.registerReceiver(this.stillDetectorBroadcastReceiver, android.content.IntentFilter(ActivityRecognizerIntentService.INTENT_ACTION_NAME))

        this.subscriptions.add(this.stillDetectorBroadcastReceiver.startDetection(1500).subscribe{
            if(it) {
                if(this.stateChangedSubject.value == TrackRecorderServiceState.Running) {
                    // this.pauseTrackingAndSetState(TrackRecorderServiceState.Paused)
                    Log.i("TRS", "StillDetector: tracking would be resumed!")
                }
            } else {
                if(this.stateChangedSubject.value == TrackRecorderServiceState.Paused) {
                    //this.resumeTracking()
                    Log.i("TRS", "StillDetector: tracking would be paused!")
                }
            }
        })
    }

    private fun tryUpdateNotification() {
        val notification = this.trackRecorderServiceNotification.update()
        if(notification != null) {
            this.service.startForeground(RefactoredTrackRecorderServiceNotification.ID, notification)
        }
    }

    private val recordingSavedSubject: PublishSubject<Nothing> = PublishSubject.create()
    public override val recordingSaved: Observable<Nothing>
        get() = this.recordingSavedSubject

    public override val trackingStartedAt: DateTime
        get() = this.trackRecording.trackingStartedAt

    public override var name: String
        get() = this.trackRecording.name
        set(value) {
            this.trackRecording.name = value
        }

    public override var comment: String?
        get() = this.trackRecording.comment
        set(value) {
            this.trackRecording.comment = value
        }

    private fun pauseTrackingAndSetState(state: TrackRecorderServiceState) {
        if (this.stateChangedSubject.value == TrackRecorderServiceState.Idle) {
            throw IllegalStateException("Tracking must be started first!")
        }

        if(this.stateChangedSubject.value == TrackRecorderServiceState.Running) {
            this.locationProvider.stopLocationUpdates()
            this.durationTimer.stop()

            this.trackRecording.pause()

            Handler().postDelayed({
                // Hack: Because processing of locationsReceived is buffered every second,
                // it can happen that by the time the tracking is saved the list of locationsReceived is still being updated.
                // To prevent exceptions, delay saving 1 second and hope updates are done; until I find a better solution!
                this.saveTracking()
            }, 1000)
        }

        this.changeState(state)
    }

    public override fun resumeTracking() {
        if(this.isDestroyed) {
            throw IllegalStateException("Object is destroyed!")
        }

        if(this.stateChangedSubject.value == TrackRecorderServiceState.Running) {
            throw IllegalStateException("Tracking must be paused first!")
        }

        this.locationProvider.startLocationUpdates()
        this.durationTimer.start()

        this.trackRecording.resume()

        this.changeState(TrackRecorderServiceState.Running)
    }

    private fun changeState(newState: TrackRecorderServiceState) {
        this.stateChangedSubject.onNext(newState)
    }

    public override fun pauseTracking() {
        if(this.isDestroyed) {
            throw IllegalStateException("Object is destroyed!")
        }

        if(this.stateChangedSubject.value != TrackRecorderServiceState.Running
            && this.stateChangedSubject.value != TrackRecorderServiceState.Idle) {
            throw IllegalStateException("Only possible in state \"Running\"!")
        }

        this.pauseTrackingAndSetState(TrackRecorderServiceState.Paused)
    }

    public override fun saveTracking() {
        if(this.isDestroyed) {
            throw IllegalStateException("Object is destroyed!")
        }

        if(this.stateChangedSubject.value == TrackRecorderServiceState.Idle
                || this.stateChangedSubject.value == TrackRecorderServiceState.Running) {
            throw IllegalStateException("Only possible in state \"Paused\" or \"LocationServicesUnavailable\"!")
        }

        this.trackService.saveTrackRecording(this.trackRecording)

        this.recordingSavedSubject.onNext(Nothing.instance)
    }

    public override fun discardTracking() {
        if(this.isDestroyed) {
            throw IllegalStateException("Object is destroyed!")
        }

        if(this.stateChangedSubject.value != TrackRecorderServiceState.Paused
            && this.stateChangedSubject.value != TrackRecorderServiceState.LocationServicesUnavailable) {
            throw IllegalStateException("Only possible in state \"Paused\" or \"LocationServicesUnavailable\"!")
        }

        this.durationTimer.reset()
        this.locationProvider.resetSequenceNumber()

        this.trackDistanceCalculator.clear()

        this.trackService.deleteTrackRecording(this.trackRecording.id.toString())

        this.changeState(TrackRecorderServiceState.Idle)

        this.destroy()
    }

    public override fun finishTracking(): TrackRecording {
        if(this.isDestroyed) {
            throw IllegalStateException("Object is destroyed!")
        }

        if(this.stateChangedSubject.value != TrackRecorderServiceState.Paused) {
            throw IllegalStateException("Only possible in state \"Paused\"!")
        }

        val finishedTrackRecording = this.trackRecording

        finishedTrackRecording.finish()
        this.saveTracking()

        this.durationTimer.reset()
        this.locationProvider.resetSequenceNumber()

        this.trackDistanceCalculator.clear()

        this.changeState(TrackRecorderServiceState.Idle)

        this.destroy()

        return finishedTrackRecording
    }

    private var isDestroyed: Boolean = false
    public override fun destroy() {
        if(this.isDestroyed) {
            return
        }

        this.service.unregisterReceiver(this.stillDetectorBroadcastReceiver)
        this.service.unregisterReceiver(this.locationAvailabilityChangedBroadcastReceiver)

        this.burnedEnergyCalculator.destroy()
        this.durationTimer.destroy()
        this.trackDistanceCalculator.destroy()
        this.locationProvider.destroy()
        this.liveLocationTrackingSession.endSession()

        this.subscriptions.dispose()

        this.recordingSavedSubject.onComplete()
        this.stateChangedSubject.onComplete()

        this.isDestroyed = true
    }
}