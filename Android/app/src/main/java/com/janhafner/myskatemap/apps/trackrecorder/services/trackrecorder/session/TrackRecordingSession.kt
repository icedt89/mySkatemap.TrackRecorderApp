package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session

import android.app.Service
import android.util.Log
import com.janhafner.myskatemap.apps.trackrecorder.BuildConfig
import com.janhafner.myskatemap.apps.trackrecorder.LocationAvailability
import com.janhafner.myskatemap.apps.trackrecorder.burnedenergy.IBurnedEnergyCalculator
import com.janhafner.myskatemap.apps.trackrecorder.burnedenergy.MetDefinition
import com.janhafner.myskatemap.apps.trackrecorder.burnedenergy.calculateBurnedEnergy
import com.janhafner.myskatemap.apps.trackrecorder.common.*
import com.janhafner.myskatemap.apps.trackrecorder.common.types.Location
import com.janhafner.myskatemap.apps.trackrecorder.common.types.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.common.types.TrackingPausedReason
import com.janhafner.myskatemap.apps.trackrecorder.common.types.TrackingResumedReason
import com.janhafner.myskatemap.apps.trackrecorder.conversion.distance.IDistanceConverterFactory
import com.janhafner.myskatemap.apps.trackrecorder.distancecalculation.IDistanceCalculator
import com.janhafner.myskatemap.apps.trackrecorder.distancecalculation.calculateDistance
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.INewLocationsAggregation
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.NewLocationsAggregation
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.TimeoutTransformer
import com.janhafner.myskatemap.apps.trackrecorder.live.LiveLocation
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.ILiveSessionController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.notifications.TrackRecorderServiceNotification
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider.ILocationProvider
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import org.joda.time.DateTime
import org.joda.time.Period
import java.util.concurrent.TimeUnit

internal final class TrackRecordingSession(private val appSettings: IAppSettings,
                                           private val distanceCalculator: IDistanceCalculator,
                                           private val distanceConverterFactory: IDistanceConverterFactory,
                                           private val trackRecording: TrackRecording,
                                           private val burnedEnergyCalculator: IBurnedEnergyCalculator,
                                           private val locationProvider: ILocationProvider,
                                           private val service: Service,
                                           private val liveSessionController: ILiveSessionController) : ITrackRecordingSession {
    private val subscriptions: CompositeDisposable = CompositeDisposable()

    public override lateinit var locationsAggregation: INewLocationsAggregation

    private val recordingTimeTimer: IObservableTimer = ObservableTimer()

    private val isStillObservableTimeout: IObservableTimeout

    public override lateinit var recordingTimeChanged: Observable<Period>
        private set

    public override lateinit var locationsChanged: Observable<Location>
        private set

    public override lateinit var burnedEnergyChanged: Observable<Float>
        private set

    public override lateinit var distanceChanged: Observable<Float>
        private set

    public override lateinit var isStillChanged: Observable<Boolean>
        private set

    public override val activityCode: String
        get() = this.trackRecording.activityCode

    private val sessionClosedSubject: Subject<ITrackRecordingSession> = PublishSubject.create()
    public override val sessionClosed: Observable<ITrackRecordingSession> = this.sessionClosedSubject.subscribeOn(Schedulers.computation())

    private val stateChangedSubject: BehaviorSubject<SessionStateInfo> = BehaviorSubject.createDefault(SessionStateInfo(TrackRecordingSessionState.Paused))
    public override val stateChanged: Observable<SessionStateInfo> = this.stateChangedSubject.subscribeOn(Schedulers.computation())

    public override val currentState: SessionStateInfo
        get() = this.stateChangedSubject.value!!

    private var trackRecorderServiceNotification: TrackRecorderServiceNotification

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

        this.locationsAggregation = NewLocationsAggregation(this.locationsChanged)

        this.recordingTimeChanged = this.recordingTimeTimer.secondElapsed
        if (trackRecording.recordingTime != Period.ZERO && trackRecording.recordingTime.seconds > 0) {
            this.recordingTimeTimer.set(trackRecording.recordingTime.seconds)
        }

        // Awesome: Because without .defer() all subscriptions would share
        // the one transformer and results would be nonsense!
        // .defer() gives each subscriptions a new one =)
        this.distanceChanged = Observable.defer {
            this.locationsChanged
                    // Buffering removed because it is not necessary in real world scenaries
                    //.buffer(250, TimeUnit.MILLISECONDS, 250)
                    //.filterNotEmpty()
                    .map {
                        listOf(it)
                    }
                    .calculateDistance(this.distanceCalculator)
        }

        this.isStillObservableTimeout = ObservableTimeout(BuildConfig.STILL_DETECTION_DETECTION_TIMEOUT_IN_MILLISECONDS.toLong())
        this.isStillChanged = this.locationsChanged
                .map {
                    false
                }
                .compose(TimeoutTransformer(this.isStillObservableTimeout) { true })

        this.initializeSession()

        this.trackRecorderServiceNotification = TrackRecorderServiceNotification(this.service, this.appSettings, this, this.distanceConverterFactory)
    }

    private fun initializeSession() {
        this.subscriptions.addAll(
                this.isStillChanged.subscribe {
                    if (it) {
                        Log.i("STILLDETECTION", "User does'nt move!")

                        if (this.currentState.state == TrackRecordingSessionState.Running) {
                            if (BuildConfig.STILL_DETECTION_ENABLE_PAUSERESUME) {
                                this.pauseTrackingAndSetState(TrackingPausedReason.StillStandDetected, false)

                                Log.i("STILLDETECTION", "Tracking was paused but location was keeped on!")
                            } else {
                                Log.i("STILLDETECTION", "Tracking was NOT paused because feature is disabled!")
                            }
                        } else {
                            Log.i("STILLDETECTION", "Tracking was NOT paused because current state is NOT running!")
                        }
                    } else {
                        Log.i("STILLDETECTION", "User is moving!")

                        if (this.currentState.state == TrackRecordingSessionState.Paused && this.currentState.pausedReason == TrackingPausedReason.StillStandDetected) {
                            if (BuildConfig.STILL_DETECTION_ENABLE_PAUSERESUME) {
                                this.resumeTrackingAndSetState(TrackingResumedReason.LeftAutomaticStillStand)

                                Log.i("STILLDETECTION", "Tracking was resumed!")
                            } else {
                                Log.i("STILLDETECTION", "Tracking was NOT resumed because feature is disabled!")
                            }
                        } else {
                            Log.i("STILLDETECTION", "Tracking was NOT resumed because current state is NOT paused or reason is NOT stillstanddetected!")
                        }
                    }
                },
                this.appSettings.propertyChanged
                        .hasChanged()
                        .subscribe {
                            if (it.propertyName == IAppSettings::enableAutoPauseOnStill.name) {
                                if (this.appSettings.enableAutoPauseOnStill) {
                                    this.tryStartAutoPauseTimeout()
                                } else {
                                    this.isStillObservableTimeout.stop()
                                }
                            } else if (it.propertyName == IAppSettings::enableLiveLocation.name) {
                                if (this.appSettings.enableLiveLocation) {
                                    this.liveSessionController.startSession()
                                } else {
                                    this.liveSessionController.endSession()
                                }
                            }
                        },
                this.recordingTimeChanged
                        .subscribe {
                            this.trackRecording.recordingTime = it
                        },
                this.locationsChanged
                        .subscribe {
                            this.locationsAggregation.add(it)

                            this.trackRecording.addLocations(listOf(it))
                        },
                this.locationsChanged
                        .map {
                            LiveLocation(it.capturedAt, it.latitude, it.longitude)
                        }
                        .buffer(1, TimeUnit.MINUTES, 20)
                        .filterNotEmpty()
                        .subscribe {
                            this.liveSessionController.sendLocations(it)
                        },
                this.stateChanged
                        .subscribe {
                            if (it.state == TrackRecordingSessionState.Running) {
                                this.tryStartAutoPauseTimeout()
                            } else if (it.pausedReason == TrackingPausedReason.UserInitiated) {
                                this.isStillObservableTimeout.stop()
                            }
                        }
        )

        if (this.trackRecording.userProfile != null) {
            val metActivityDefinition = MetDefinition.getMetDefinitionByCode(this.trackRecording.activityCode)
            if (metActivityDefinition != null) {
                this.burnedEnergyChanged = this.recordingTimeChanged
                        .map {
                            it.seconds
                        }
                        .calculateBurnedEnergy(this.burnedEnergyCalculator, this.trackRecording.userProfile!!.weightInKilograms,
                                this.trackRecording.userProfile!!.heightInCentimeters,
                                this.trackRecording.userProfile!!.age,
                                this.trackRecording.userProfile!!.sex,
                                metActivityDefinition.value)
            }
        }

        this.initializeLocationAvailabilityChangedBroadcastReceiver()
        this.tryStartAutoPauseTimeout()

        if (this.appSettings.enableLiveLocation) {
            this.liveSessionController.startSession()
        }
    }

    private fun initializeLocationAvailabilityChangedBroadcastReceiver() {
        this.subscriptions.add(
                LocationAvailability.changed(this.service)
                        .subscribeOn(AndroidSchedulers.mainThread())
                .distinctUntilChanged()
                .subscribe {
                    val currentState = this.stateChangedSubject.value!!

                    if (it) {
                        if (currentState.pausedReason == TrackingPausedReason.LocationServicesUnavailable) {
                            this.pauseTrackingAndSetState(TrackingPausedReason.UserInitiated)
                        }
                    } else {
                        if (currentState.pausedReason != TrackingPausedReason.LocationServicesUnavailable) {
                            this.pauseTrackingAndSetState(TrackingPausedReason.LocationServicesUnavailable)
                        }
                    }
                })
    }

    private fun tryStartAutoPauseTimeout() {
        if (!this.appSettings.enableAutoPauseOnStill) {
            return
        }

        this.isStillObservableTimeout.restart()
    }

    public override val trackingStartedAt: DateTime
        get() = this.trackRecording.startedAt

    public override fun resumeTracking() {
        if (this.isDestroyed) {
            throw ObjectDestroyedException()
        }

        if (this.stateChangedSubject.value!!.state == TrackRecordingSessionState.Running) {
            throw IllegalStateException("Tracking must be paused first!")
        }

        this.resumeTrackingAndSetState(TrackingResumedReason.ManuallyResumed)
    }

    private fun changeState(newState: TrackRecordingSessionState, pausedReason: TrackingPausedReason?) {
        this.stateChangedSubject.onNext(SessionStateInfo(newState, pausedReason))
    }

    public override fun pauseTracking() {
        if (this.isDestroyed) {
            throw ObjectDestroyedException()
        }

        if (this.stateChangedSubject.value!!.state != TrackRecordingSessionState.Running) {
            throw IllegalStateException("Only possible in state \"${TrackRecordingSessionState.Running}\"!")
        }

        this.pauseTrackingAndSetState(TrackingPausedReason.UserInitiated)
    }

    private fun resumeTrackingAndSetState(resumedReason: TrackingResumedReason) {
        if (!this.locationProvider.isActive) {
            this.locationProvider.startLocationUpdates()
        }

        this.recordingTimeTimer.start()

        this.trackRecording.resume(resumedReason)

        this.changeState(TrackRecordingSessionState.Running, null)
    }

    private fun pauseTrackingAndSetState(pausedReason: TrackingPausedReason, stopLocationProvider: Boolean = true) {
        if (this.stateChangedSubject.value!!.state == TrackRecordingSessionState.Running) {
            if (this.locationProvider.isActive && stopLocationProvider) {
                this.locationProvider.stopLocationUpdates()
            }

            this.recordingTimeTimer.stop()

            this.trackRecording.pause(pausedReason)
        }

        this.changeState(TrackRecordingSessionState.Paused, pausedReason)
    }

    public override fun discardTracking() {
        if (this.isDestroyed) {
            throw ObjectDestroyedException()
        }

        if (this.stateChangedSubject.value!!.state != TrackRecordingSessionState.Paused) {
            throw IllegalStateException("Only possible in state \"${TrackRecordingSessionState.Paused}\"!")
        }

        this.recordingTimeTimer.reset()

        this.destroy()
    }

    public override fun finishTracking(): TrackRecording {
        if (this.isDestroyed) {
            throw ObjectDestroyedException()
        }

        if (this.stateChangedSubject.value!!.state != TrackRecordingSessionState.Paused) {
            throw IllegalStateException("Only possible in state \"${TrackRecordingSessionState.Paused}\"!")
        }

        val finishedTrackRecording = this.trackRecording

        finishedTrackRecording.finish()

        this.recordingTimeTimer.reset()

        this.destroy()

        return finishedTrackRecording
    }

    private var isDestroyed: Boolean = false
    public override fun destroy() {
        if (this.isDestroyed) {
            return
        }

        this.isStillObservableTimeout.destroy()
        this.liveSessionController.endSession()

        this.trackRecorderServiceNotification.destroy()

        this.locationsAggregation.destroy()
        this.recordingTimeTimer.destroy()
        this.locationProvider.destroy()

        this.subscriptions.dispose()

        this.stateChangedSubject.onComplete()

        this.isDestroyed = true

        this.sessionClosedSubject.onNext(this)
        this.sessionClosedSubject.onComplete()
    }
}