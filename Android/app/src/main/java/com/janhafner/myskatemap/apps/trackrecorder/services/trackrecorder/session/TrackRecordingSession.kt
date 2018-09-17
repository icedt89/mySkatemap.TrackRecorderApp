package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session

import android.app.Service
import com.janhafner.myskatemap.apps.trackrecorder.aggregations.ILocationsAggregation
import com.janhafner.myskatemap.apps.trackrecorder.common.IObservableTimer
import com.janhafner.myskatemap.apps.trackrecorder.common.filterNotEmpty
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance.IDistanceConverterFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.burnedenergy.BurnedEnergy
import com.janhafner.myskatemap.apps.trackrecorder.services.burnedenergy.IBurnedEnergyCalculator
import com.janhafner.myskatemap.apps.trackrecorder.services.calculateMissingSpeed
import com.janhafner.myskatemap.apps.trackrecorder.services.distance.IDistanceCalculator
import com.janhafner.myskatemap.apps.trackrecorder.services.locationavailability.ILocationAvailabilityChangedDetector
import com.janhafner.myskatemap.apps.trackrecorder.services.models.Location
import com.janhafner.myskatemap.apps.trackrecorder.services.models.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.notifications.TrackRecorderServiceNotification
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider.ILocationProvider
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.stilldetection.IStillDetector
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import org.joda.time.DateTime
import org.joda.time.Period
import java.util.concurrent.TimeUnit

// Thats a shitload of dependencies...
internal final class TrackRecordingSession(private val appSettings: IAppSettings,
                                           private val trackRecorderServiceNotification: TrackRecorderServiceNotification,
                                           private val distanceCalculator: IDistanceCalculator,
                                           private val distanceConverterFactory: IDistanceConverterFactory,
                                           private val trackRecording: TrackRecording,
                                           public override val locationsAggregation: ILocationsAggregation,
                                           private val burnedEnergyCalculator: IBurnedEnergyCalculator,
                                           private val locationProvider: ILocationProvider,
                                           private val recordingTimeTimer: IObservableTimer,
                                           private val service: Service,
                                           private val stillDetector: IStillDetector,
                                           private val locationAvailabilityChangedDetector: ILocationAvailabilityChangedDetector) : ITrackRecordingSession {
    private val subscriptions: CompositeDisposable = CompositeDisposable()

    public override lateinit var recordingTimeChanged: Observable<Period>
        private set

    public override lateinit var locationsChanged: Observable<Location>
        private set

    public override val burnedEnergyChanged: Observable<BurnedEnergy> = this.burnedEnergyCalculator.calculatedValueChanged

    public override val distanceChanged: Observable<Float> = this.distanceCalculator.distanceCalculated

    private val sessionClosedSubject: Subject<ITrackRecordingSession> = PublishSubject.create()
    public override val sessionClosed: Observable<ITrackRecordingSession> = this.sessionClosedSubject.subscribeOn(Schedulers.computation())

    private val stateChangedSubject: BehaviorSubject<SessionStateInfo> = BehaviorSubject.createDefault(SessionStateInfo(TrackRecordingSessionState.Paused))
    public override val stateChanged: Observable<SessionStateInfo> = this.stateChangedSubject.subscribeOn(Schedulers.computation())

    public override val currentState: SessionStateInfo
        get() = this.stateChangedSubject.value!!

    init {
        this.locationsChanged = this.locationProvider.locationsReceived
                .calculateMissingSpeed()
                .replay()
                .autoConnect()
        if (trackRecording.locations.any()) {
            val sortedLocations = trackRecording.locations.sortedBy {
                it.capturedAt
            }

            this.locationsChanged = locationsChanged.startWith(sortedLocations)
        }

        this.recordingTimeChanged = this.recordingTimeTimer.secondElapsed
        if (trackRecording.recordingTime != Period.ZERO && trackRecording.recordingTime.seconds > 0) {
            this.recordingTimeTimer.set(trackRecording.recordingTime.seconds)
        }

        this.initializeSession()
    }

    private fun initializeSession() {
        this.subscriptions.addAll(
                this.appSettings.propertyChanged
                        .filter {
                            it.hasChanged
                        }
                        .subscribe {
                            if (it.propertyName == IAppSettings::vibrateOnLocationAvailabilityLoss.name) {
                                this.trackRecorderServiceNotification.vibrateOnLocationAvailabilityLoss = it.newValue as Boolean

                                this.tryUpdateNotification()
                            } else if (it.propertyName == IAppSettings::distanceConverterTypeName.name) {
                                this.trackRecorderServiceNotification.distanceConverter = this.distanceConverterFactory.createConverter()

                                this.tryUpdateNotification()
                            } else if(it.propertyName == IAppSettings::enableAutoPauseOnStill.name) {
                                if(this.appSettings.enableAutoPauseOnStill) {
                                    this.tryRegisterStillDetectorBroadcastReceiver()
                                } else {
                                    this.tryUnregisterStillDetectorBroadcastReceiver()
                                }
                            }
                        },
                this.recordingTimeChanged
                        .subscribe {
                            this.burnedEnergyCalculator.calculate(it.seconds)

                            this.trackRecording.recordingTime = it

                            this.trackRecorderServiceNotification.recordingTime = it

                            this.tryUpdateNotification()
                        },
                this.locationsChanged
                        .buffer(1, TimeUnit.SECONDS)
                        .filterNotEmpty()
                        .subscribe {
                            this.locationsAggregation.addAll(it)

                            this.trackRecording.locations.addAll(it)

                            this.distanceCalculator.addAll(it)
                        },
                this.stateChanged
                        .subscribe {
                            this.trackRecorderServiceNotification.state = it

                            this.tryUpdateNotification()

                            if (it.state == TrackRecordingSessionState.Running) {
                                this.tryRegisterStillDetectorBroadcastReceiver()
                            } else if(it.pausedReason == TrackingPausedReason.UserInitiated) {
                                this.tryUnregisterStillDetectorBroadcastReceiver()
                            }
                        },
                this.distanceChanged
                        .subscribe {
                            this.trackRecorderServiceNotification.distance = it

                            this.tryUpdateNotification()
                        })

        this.initializeLocationAvailabilityChangedBroadcastReceiver()
    }

    private fun initializeLocationAvailabilityChangedBroadcastReceiver() {
        this.subscriptions.add(this.locationAvailabilityChangedDetector.locationAvailabilityChanged
                .subscribe {
                    val currentState = this.stateChangedSubject.value!!

                    if (it){
                        if(currentState.pausedReason == TrackingPausedReason.LocationServicesUnavailable){
                            this.pauseTrackingAndSetState(TrackingPausedReason.UserInitiated)
                        }
                    } else {
                        if (currentState.pausedReason != TrackingPausedReason.LocationServicesUnavailable) {
                            this.pauseTrackingAndSetState(TrackingPausedReason.LocationServicesUnavailable)
                        }
                    }
                })
    }

    private fun tryRegisterStillDetectorBroadcastReceiver() {
        if(this.stillDetector.isDetecting) {
            return
        }

        this.subscriptions.add(this.stillDetector.stillDetected
                .subscribe {
                    val currentState = this.stateChangedSubject.value!!

                    if(it) {
                        if (currentState.state == TrackRecordingSessionState.Running) {
                            // this.pauseTrackingAndSetState(TrackingPausedReason.StillStandDetected)
                        }
                    } else {
                        if (currentState.state == TrackRecordingSessionState.Paused
                                && currentState.pausedReason == TrackingPausedReason.StillStandDetected) {
                            // this.resumeTracking()
                        }
                    }
                })

        this.stillDetector.startDetection()
    }

    private fun tryUnregisterStillDetectorBroadcastReceiver() {
        if(!this.stillDetector.isDetecting) {
            return
        }

        this.stillDetector.stopDetection()
    }

    private fun tryUpdateNotification() {
        val notification = this.trackRecorderServiceNotification.update()
        if (notification != null) {
            this.service.startForeground(TrackRecorderServiceNotification.ID, notification)
        }
    }

    public override val trackingStartedAt: DateTime
        get() = this.trackRecording.trackingStartedAt

    public override fun resumeTracking() {
        if (this.isDestroyed) {
            throw IllegalStateException("Object is destroyed!")
        }

        if (this.stateChangedSubject.value!!.state == TrackRecordingSessionState.Running) {
            throw IllegalStateException("Tracking must be paused first!")
        }

        this.locationProvider.startLocationUpdates()
        this.recordingTimeTimer.start()

        this.trackRecording.resume()

        this.changeState(TrackRecordingSessionState.Running)
    }

    private fun changeState(newState: TrackRecordingSessionState, pausedReason: TrackingPausedReason = TrackingPausedReason.JustInitialized) {
        this.stateChangedSubject.onNext(SessionStateInfo(newState, pausedReason))
    }

    public override fun pauseTracking() {
        if (this.isDestroyed) {
            throw IllegalStateException("Object is destroyed!")
        }

        if (this.stateChangedSubject.value!!.state != TrackRecordingSessionState.Running) {
            throw IllegalStateException("Only possible in state \"${TrackRecordingSessionState.Running}\"!")
        }

        this.pauseTrackingAndSetState(TrackingPausedReason.UserInitiated)
    }

    private fun pauseTrackingAndSetState(pausedReason: TrackingPausedReason) {
        if (this.stateChangedSubject.value!!.state == TrackRecordingSessionState.Running) {
            this.locationProvider.stopLocationUpdates()
            this.recordingTimeTimer.stop()

            this.trackRecording.pause()
        }

        this.changeState(TrackRecordingSessionState.Paused, pausedReason)
    }

    public override fun discardTracking() {
        if (this.isDestroyed) {
            throw IllegalStateException("Object is destroyed!")
        }

        if (this.stateChangedSubject.value!!.state != TrackRecordingSessionState.Paused) {
            throw IllegalStateException("Only possible in state \"${TrackRecordingSessionState.Paused}\"!")
        }

        this.recordingTimeTimer.reset()

        this.distanceCalculator.clear()

        this.destroy()
    }

    public override fun finishTracking(): TrackRecording {
        if (this.isDestroyed) {
            throw IllegalStateException("Object is destroyed!")
        }

        if (this.stateChangedSubject.value!!.state != TrackRecordingSessionState.Paused) {
            throw IllegalStateException("Only possible in state \"${TrackRecordingSessionState.Paused}\"!")
        }

        val finishedTrackRecording = this.trackRecording

        finishedTrackRecording.finish()

        this.recordingTimeTimer.reset()

        this.distanceCalculator.clear()

        this.destroy()

        return finishedTrackRecording
    }

    private var isDestroyed: Boolean = false
    public override fun destroy() {
        if (this.isDestroyed) {
            return
        }

        this.tryUnregisterStillDetectorBroadcastReceiver()

        this.locationsAggregation.destroy()
        this.recordingTimeTimer.destroy()
        this.burnedEnergyCalculator.destroy()
        this.distanceCalculator.destroy()
        this.locationProvider.destroy()

        this.subscriptions.dispose()

        this.stateChangedSubject.onComplete()

        this.isDestroyed = true

        this.sessionClosedSubject.onNext(this)
        this.sessionClosedSubject.onComplete()
    }
}