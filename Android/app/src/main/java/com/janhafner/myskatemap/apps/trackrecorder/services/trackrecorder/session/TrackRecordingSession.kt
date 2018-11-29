package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session

import android.app.Service
import android.util.Log
import com.janhafner.myskatemap.apps.trackrecorder.BuildConfig
import com.janhafner.myskatemap.apps.trackrecorder.activitydetection.ActivityDetectorBroadcastReceiver
import com.janhafner.myskatemap.apps.trackrecorder.activitydetection.ActivityType
import com.janhafner.myskatemap.apps.trackrecorder.activitydetection.IActivityDetectorSource
import com.janhafner.myskatemap.apps.trackrecorder.burnedenergy.IBurnedEnergyCalculator
import com.janhafner.myskatemap.apps.trackrecorder.burnedenergy.MetDefinition
import com.janhafner.myskatemap.apps.trackrecorder.burnedenergy.toObservableTransformer
import com.janhafner.myskatemap.apps.trackrecorder.common.*
import com.janhafner.myskatemap.apps.trackrecorder.common.types.Location
import com.janhafner.myskatemap.apps.trackrecorder.common.types.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.conversion.distance.IDistanceConverterFactory
import com.janhafner.myskatemap.apps.trackrecorder.distancecalculation.IDistanceCalculator
import com.janhafner.myskatemap.apps.trackrecorder.distancecalculation.toObservableTransformer
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.ILocationsAggregation
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.LocationsAggregation
import com.janhafner.myskatemap.apps.trackrecorder.live.LiveLocation
import com.janhafner.myskatemap.apps.trackrecorder.locationavailability.ILocationAvailabilityChangedSource
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.ILiveSessionController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.notifications.TrackRecorderServiceNotification
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider.ILocationProvider
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.StillPlayground.StillPlaygroundActivity
import io.reactivex.Observable
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
                                           private val activityDetectorBroadcastReceiver: ActivityDetectorBroadcastReceiver,
                                           private val activityDetectorSource: IActivityDetectorSource,
                                           private val locationAvailabilityChangedSource: ILocationAvailabilityChangedSource,
                                           private val liveSessionController: ILiveSessionController) : ITrackRecordingSession {
    private val subscriptions: CompositeDisposable = CompositeDisposable()

    public override val locationsAggregation: ILocationsAggregation = LocationsAggregation()

    private val recordingTimeTimer: IObservableTimer = ObservableTimer()

    public override lateinit var recordingTimeChanged: Observable<Period>
        private set

    public override lateinit var locationsChanged: Observable<Location>
        private set

    public override lateinit var burnedEnergyChanged: Observable<Float>
        private set

    public override lateinit var distanceChanged: Observable<Float>
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

        this.recordingTimeChanged = this.recordingTimeTimer.secondElapsed
        if (trackRecording.recordingTime != Period.ZERO && trackRecording.recordingTime.seconds > 0) {
            this.recordingTimeTimer.set(trackRecording.recordingTime.seconds)
        }

        // Awesome: Because without .defer() all subscriptions would share
        // the one transformer and results would be nonsense!
        // .defer() gives each subscriptions a new one =)
        this.distanceChanged = Observable.defer {
            this.locationsChanged
                    .buffer(250, TimeUnit.MILLISECONDS, 250)
                    .filterNotEmpty()
                    .compose(this.distanceCalculator.toObservableTransformer())
        }

        this.initializeSession()

        this.trackRecorderServiceNotification = TrackRecorderServiceNotification(this.service, this.appSettings, this, this.distanceConverterFactory)
    }

    private fun initializeSession() {
        this.subscriptions.addAll(
                this.appSettings.propertyChanged
                        .hasChanged()
                        .subscribe {
                            if(it.propertyName == IAppSettings::enableAutoPauseOnStill.name) {
                                if(this.appSettings.enableAutoPauseOnStill) {
                                    this.tryRegisterActivityDetectorBroadcastReceiver()
                                } else {
                                    this.tryUnregisterActivityDetectorBroadcastReceiver()
                                }
                            } else if(it.propertyName == IAppSettings::enableLiveLocation.name) {
                                if(this.appSettings.enableLiveLocation) {
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
                                this.tryRegisterActivityDetectorBroadcastReceiver()
                            } else if(it.pausedReason == TrackingPausedReason.UserInitiated) {
                                this.tryUnregisterActivityDetectorBroadcastReceiver()
                            }
                        }
        )

        if(this.trackRecording.userProfile != null) {
            val metActivityDefinition = MetDefinition.getMetDefinitionByCode(this.trackRecording.activityCode)
            if (metActivityDefinition != null) {
                this.burnedEnergyChanged = this.recordingTimeChanged
                            .map{
                                it.seconds
                            }
                            .compose(this.burnedEnergyCalculator.toObservableTransformer(this.trackRecording.userProfile!!.weightInKilograms,
                                this.trackRecording.userProfile!!.heightInCentimeters,
                                this.trackRecording.userProfile!!.age,
                                this.trackRecording.userProfile!!.sex,
                                metActivityDefinition.value))
            }
        }

        this.initializeLocationAvailabilityChangedBroadcastReceiver()
        this.tryRegisterActivityDetectorBroadcastReceiver()

        if(this.appSettings.enableLiveLocation){
            this.liveSessionController.startSession()
        }
    }

    private fun initializeLocationAvailabilityChangedBroadcastReceiver() {
        this.subscriptions.add(this.locationAvailabilityChangedSource.locationAvailable
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

    private fun tryRegisterActivityDetectorBroadcastReceiver() {
        if(this.activityDetectorBroadcastReceiver.isDetecting || !this.appSettings.enableAutoPauseOnStill) {
            return
        }

        val timeoutTimewindow = BuildConfig.FUSED_LOCATION_PROVIDER_INTERVAL_IN_MILLISECONDS
                .times(2)
                .coerceAtMost(BuildConfig.FUSED_LOCATION_PROVIDER_MAX_WAIT_TIME_IN_MILLISECONDS)
                .toLong()
        this.subscriptions.add(this.locationsChanged
                .map {
                    false
                }
                .debounce(2, TimeUnit.SECONDS)
                .compose(StillPlaygroundActivity.TimeoutTransformer(timeoutTimewindow, { true }, TimeUnit.MILLISECONDS))
                .subscribe {
                    if (it) {
                        Log.i("IS_PG", "User does'nt move!")
                    } else {
                        Log.i("IS_PG", "User is moving!")
                    }
                })

        this.subscriptions.add(this.activityDetectorSource.activityDetected
                .subscribe {
                    val currentState = this.stateChangedSubject.value!!

                    Log.i("ADBR", "Receiving detected activity ${it}. Current State is ${currentState.state} ${currentState.pausedReason}")

                    if(it == ActivityType.Still) {
                        Log.i("ADRB", "Still detected")

                        if (currentState.state == TrackRecordingSessionState.Running) {
                            Log.i("ADBR", "Pausing tracking")

                            if(BuildConfig.STILL_DETECTION_ENABLE_PAUSERESUME){
                                this.pauseTrackingAndSetState(TrackingPausedReason.StillStandDetected)
                            }
                        }
                    } else {
                        Log.i("ADBR", "No still detected")

                        if (currentState.state == TrackRecordingSessionState.Paused
                                && currentState.pausedReason == TrackingPausedReason.StillStandDetected) {
                            Log.i("ADBR", "Resuming tracking")

                            if(BuildConfig.STILL_DETECTION_ENABLE_PAUSERESUME) {
                                this.resumeTracking()
                            }
                        }
                    }
                })

        this.activityDetectorBroadcastReceiver.startDetection()
    }

    private fun tryUnregisterActivityDetectorBroadcastReceiver() {
        if(!this.activityDetectorBroadcastReceiver.isDetecting) {
            return
        }

        this.activityDetectorBroadcastReceiver.stopDetection()
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

        if (!this.locationProvider.isActive) {
            this.locationProvider.startLocationUpdates()
        }

        this.recordingTimeTimer.start()

        this.trackRecording.resume()

        this.changeState(TrackRecordingSessionState.Running)
    }

    private fun changeState(newState: TrackRecordingSessionState, pausedReason: TrackingPausedReason = TrackingPausedReason.JustInitialized) {
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

    private fun pauseTrackingAndSetState(pausedReason: TrackingPausedReason, stopLocationProvider: Boolean = true) {
        if (this.stateChangedSubject.value!!.state == TrackRecordingSessionState.Running) {
            if (this.locationProvider.isActive && stopLocationProvider) {
                this.locationProvider.stopLocationUpdates()
            }

            this.recordingTimeTimer.stop()

            this.trackRecording.pause()
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

        this.tryUnregisterActivityDetectorBroadcastReceiver()
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