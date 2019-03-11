package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session

import com.janhafner.myskatemap.apps.trackrecorder.BuildConfig
import com.janhafner.myskatemap.apps.trackrecorder.conversion.distance.IDistanceConverterFactory
import com.janhafner.myskatemap.apps.trackrecorder.core.*
import com.janhafner.myskatemap.apps.trackrecorder.core.types.Location
import com.janhafner.myskatemap.apps.trackrecorder.core.types.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.core.types.TrackingPausedReason
import com.janhafner.myskatemap.apps.trackrecorder.core.types.TrackingResumedReason
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.ILocationsAggregation
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.LocationsAggregation
import com.janhafner.myskatemap.apps.trackrecorder.live.LiveLocation
import com.janhafner.myskatemap.apps.trackrecorder.locationServicesAvailabilityChanged
import com.janhafner.myskatemap.apps.trackrecorder.services.track.ITrackService
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.ILiveSessionController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderService
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.notifications.TrackRecorderServiceNotification
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider.ILocationProvider
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import org.joda.time.DateTime
import org.joda.time.Period
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

internal final class TrackRecordingSession(private val appSettings: IAppSettings,
                                           private val distanceConverterFactory: IDistanceConverterFactory,
                                           private val trackRecording: TrackRecording,
                                           private val locationProvider: ILocationProvider,
                                           private val service: TrackRecorderService,
                                           private val trackService: ITrackService,
                                           private val liveSessionController: ILiveSessionController) : ITrackRecordingSession {
    private val subscriptions: CompositeDisposable = CompositeDisposable()

    public override lateinit var locationsAggregation: ILocationsAggregation

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

    private val stateChangedSubject: BehaviorSubject<SessionStateInfo> = BehaviorSubject.createDefault(SessionStateInfo(TrackRecordingSessionState.Paused))
    public override val stateChanged: Observable<SessionStateInfo> = this.stateChangedSubject

    public override val currentState: SessionStateInfo
        get() = this.stateChangedSubject.value!!

    private val currentSegmentNumber = AtomicInteger()

    private val locationsReceivedWithCurrentSegmentNumberCount = AtomicInteger()

    private var trackRecorderServiceNotification: TrackRecorderServiceNotification

    init {
        this.locationsChanged = this.locationProvider.locationsReceived
                .doOnNext {
                    it.segmentNumber = this.currentSegmentNumber.get()
                    this.locationsReceivedWithCurrentSegmentNumberCount.incrementAndGet()
                }
                .calculateMissingSpeed()
                .replay()
                .autoConnect()
        if (trackRecording.locations.any()) {
            val sortedLocations = trackRecording.locations.sortedBy {
                it.time
            }

            this.currentSegmentNumber.set(sortedLocations.maxBy { it.segmentNumber }!!.segmentNumber)

            this.locationsChanged = locationsChanged.startWith(sortedLocations)
        }

        this.locationsAggregation = LocationsAggregation(this.locationsChanged)

        this.recordingTimeChanged = this.recordingTimeTimer.secondElapsed
        if (trackRecording.recordingTime != Period.ZERO && trackRecording.recordingTime.seconds > 0) {
            this.recordingTimeTimer.set(trackRecording.recordingTime.seconds)
        }

        this.distanceChanged = this.locationsChanged.map {
            listOf(it.toLiteAndroidLocation())
        }
        .distance()
        .replay(1)
        .autoConnect()

        this.initializeSession()

        this.trackRecorderServiceNotification = TrackRecorderServiceNotification(this.service, this.appSettings, this, this.distanceConverterFactory)
    }

    private fun initializeSession() {
        // TODO: Test speedBasedAutopauseStrategy
        val distanceBasedAutopauseStrategy = this.locationsChanged
                .map {
                    it.toLiteAndroidLocation()
                }
                .inDistance(BuildConfig.STILL_DETECTION_SMALLEST_DISPLACEMENT_IN_METERS)
                .debounce(BuildConfig.STILL_DETECTION_DETECTION_TIMEOUT_IN_MILLISECONDS.toLong(), TimeUnit.MILLISECONDS)
                .filter {
                    this.currentState.state == TrackRecordingSessionState.Running
                }
                .map {
                    true
                }
        val speedBasedAutopauseStrategy = this.locationsChanged
                .fasterThan(BuildConfig.STILL_DETECTION_SMALLEST_DISPLACEMENT_IN_METERS)
                .debounce(BuildConfig.STILL_DETECTION_DETECTION_TIMEOUT_IN_MILLISECONDS.toLong(), TimeUnit.MILLISECONDS)
                .filter {
                    this.currentState.state == TrackRecordingSessionState.Running
                }
                .map {
                    true
                }

        this.subscriptions.addAll(
                this.locationsChanged
                        .subscribeOn(Schedulers.computation())
                        .filter {
                            this.appSettings.enableAutoPauseOnStill
                        }
                        .map {
                            false
                        }
                        .mergeWith(distanceBasedAutopauseStrategy)
                        .subscribe {
                            if (it) {
                                if (this.currentState.state == TrackRecordingSessionState.Running) {
                                    this.pauseTrackingAndSetState(TrackingPausedReason.StillStandDetected, false)
                                }
                            } else {
                                if (this.currentState.state == TrackRecordingSessionState.Paused && this.currentState.pausedReason == TrackingPausedReason.StillStandDetected) {
                                    this.resumeTrackingAndSetState(TrackingResumedReason.LeftAutomaticStillStand)
                                }
                            }
                        },
                this.appSettings.propertyChanged
                        .subscribeOn(Schedulers.computation())
                        .hasChanged()
                        .isNamed(IAppSettings::enableLiveLocation.name)
                        .startWith(PropertyChangedData(IAppSettings::enableLiveLocation.name, !this.appSettings.enableLiveLocation, this.appSettings.enableLiveLocation))
                        .flatMap {
                            if (this.appSettings.enableLiveLocation) {
                                this.liveSessionController.startSession()
                                        .onErrorReturn { }
                                        .toObservable()
                            } else {
                                this.liveSessionController.endSession()
                                        .onErrorReturn { }
                                        .toObservable()
                            }
                        }
                        .retry(3)
                        .subscribe(),
                this.recordingTimeChanged
                        .subscribeOn(Schedulers.computation())
                        .subscribe {
                            this.trackRecording.recordingTime = it
                        },
                this.locationsChanged
                        .subscribeOn(Schedulers.computation())
                        .subscribe {
                            this.trackRecording.addLocations(listOf(it))
                        },
                this.locationsChanged
                        .subscribeOn(Schedulers.computation())
                        .map {
                            LiveLocation(it.time, it.latitude, it.longitude)
                        }
                        .buffer(30, TimeUnit.SECONDS, 20)
                        .filterNotEmpty()
                        .observeOn(Schedulers.io())
                        .flatMap {
                            this.liveSessionController.sendLocations(it)
                                    .subscribeOn(Schedulers.io())
                                    .onErrorReturn { }
                                    .toObservable()
                        }
                        .subscribe()
        )

        if (this.trackRecording.userProfile != null) {
            val metActivityDefinition = MetDefinition.getMetDefinitionByCode(this.trackRecording.activityCode)
            if (metActivityDefinition != null) {
                this.burnedEnergyChanged = this.recordingTimeChanged
                        .map {
                            it.seconds
                        }
                        .burnedEnergy(this.trackRecording.userProfile!!.weightInKilograms,
                                this.trackRecording.userProfile!!.heightInCentimeters,
                                this.trackRecording.userProfile!!.age,
                                this.trackRecording.userProfile!!.sex,
                                metActivityDefinition.value)
            }
        }

        this.initializeLocationAvailabilityChangedBroadcastReceiver()
    }

    private fun initializeLocationAvailabilityChangedBroadcastReceiver() {
        this.subscriptions.add(
                this.service.locationServicesAvailabilityChanged()
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
            if(this.locationsReceivedWithCurrentSegmentNumberCount.get() > 1) {
                this.currentSegmentNumber.incrementAndGet()
                this.locationsReceivedWithCurrentSegmentNumberCount.set(0)
            }

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

        this.destroy()
    }

    public override fun finishTracking(): Single<TrackRecording> {
        if (this.isDestroyed) {
            throw ObjectDestroyedException()
        }

        if (this.stateChangedSubject.value!!.state != TrackRecordingSessionState.Paused) {
            throw IllegalStateException("Only possible in state \"${TrackRecordingSessionState.Paused}\"!")
        }

        val finishedTrackRecording = this.trackRecording

        return Single.create<TrackRecording> {
            finishedTrackRecording.finish()

            it.onSuccess(finishedTrackRecording)
        }
        .flatMap {
            val trackRecording = it

    this.trackService.saveTrackRecording(trackRecording)
            .flatMap {
                Single.just(trackRecording)
            }
        }
        .doOnSuccess {
            this.destroy()
        }
    }

    private var isDestroyed: Boolean = false
    public override fun destroy() {
        if (this.isDestroyed) {
            return
        }

        this.locationsAggregation.destroy()
        this.liveSessionController.endSession()
                .subscribeOn(Schedulers.io())
                .onErrorReturn { }
                .subscribe()

        this.trackRecorderServiceNotification.destroy()

        this.recordingTimeTimer.destroy()

        this.subscriptions.dispose()

        this.stateChangedSubject.onComplete()

        this.isDestroyed = true

        this.service.currentSession = null
    }
}