package com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.session

import com.janhafner.myskatemap.apps.activityrecorder.BuildConfig
import com.janhafner.myskatemap.apps.activityrecorder.conversion.distance.IDistanceConverterFactory
import com.janhafner.myskatemap.apps.activityrecorder.core.*
import com.janhafner.myskatemap.apps.activityrecorder.core.types.Activity
import com.janhafner.myskatemap.apps.activityrecorder.core.types.Location
import com.janhafner.myskatemap.apps.activityrecorder.core.types.TrackingPausedReason
import com.janhafner.myskatemap.apps.activityrecorder.core.types.TrackingResumedReason
import com.janhafner.myskatemap.apps.activityrecorder.infrastructure.ILocationsAggregation
import com.janhafner.myskatemap.apps.activityrecorder.infrastructure.LocationsAggregation
import com.janhafner.myskatemap.apps.activityrecorder.live.LiveLocation
import com.janhafner.myskatemap.apps.activityrecorder.locationServicesAvailabilityChanged
import com.janhafner.myskatemap.apps.activityrecorder.services.activity.IActivityService
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.ActivityRecorderService
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.ILiveSessionController
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.notifications.ActivityRecorderServiceNotification
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.provider.ILocationProvider
import com.janhafner.myskatemap.apps.activityrecorder.settings.IAppSettings
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

internal final class ActivitySession(private val appSettings: IAppSettings,
                                     private val distanceConverterFactory: IDistanceConverterFactory,
                                     private val activity: Activity,
                                     private val locationProvider: ILocationProvider,
                                     private val service: ActivityRecorderService,
                                     private val activityService: IActivityService,
                                     private val liveSessionController: ILiveSessionController) : IActivitySession {
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
        get() = this.activity.activityCode

    private val stateChangedSubject: BehaviorSubject<SessionStateInfo> = BehaviorSubject.createDefault(SessionStateInfo(ActivitySessionState.Paused))
    public override val stateChanged: Observable<SessionStateInfo> = this.stateChangedSubject

    public override val currentState: SessionStateInfo
        get() = this.stateChangedSubject.value!!

    private val currentSegmentNumber = AtomicInteger()

    private val locationsReceivedWithCurrentSegmentNumberCount = AtomicInteger()

    private var activityRecorderServiceNotification: ActivityRecorderServiceNotification

    init {
        this.locationsChanged = this.locationProvider.locationsReceived
                .doOnNext {
                    it.segmentNumber = this.currentSegmentNumber.get()
                    this.locationsReceivedWithCurrentSegmentNumberCount.incrementAndGet()
                }
                .calculateMissingSpeed()
                .replay()
                .autoConnect()
        if (activity.locations.any()) {
            val sortedLocations = activity.locations.sortedBy {
                it.time
            }

            this.currentSegmentNumber.set(sortedLocations.maxBy { it.segmentNumber }!!.segmentNumber)

            this.locationsChanged = locationsChanged.startWith(sortedLocations)
        }

        this.locationsAggregation = LocationsAggregation(this.locationsChanged)

        this.recordingTimeChanged = this.recordingTimeTimer.secondElapsed
        if (activity.recordingTime != Period.ZERO && activity.recordingTime.seconds > 0) {
            this.recordingTimeTimer.set(activity.recordingTime.seconds)
        }

        this.distanceChanged = this.locationsChanged.map {
            listOf(it.toLiteAndroidLocation())
        }
        .distance()
        .replay(1)
        .autoConnect()

        this.initializeSession()

        this.activityRecorderServiceNotification = ActivityRecorderServiceNotification(this.service, this.appSettings, this, this.distanceConverterFactory)
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
                    this.currentState.state == ActivitySessionState.Running
                }
                .map {
                    true
                }
        val speedBasedAutopauseStrategy = this.locationsChanged
                .fasterThan(BuildConfig.STILL_DETECTION_SMALLEST_DISPLACEMENT_IN_METERS)
                .debounce(BuildConfig.STILL_DETECTION_DETECTION_TIMEOUT_IN_MILLISECONDS.toLong(), TimeUnit.MILLISECONDS)
                .filter {
                    this.currentState.state == ActivitySessionState.Running
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
                                if (this.currentState.state == ActivitySessionState.Running) {
                                    this.pauseTrackingAndSetState(TrackingPausedReason.StillStandDetected, false)
                                }
                            } else {
                                if (this.currentState.state == ActivitySessionState.Paused && this.currentState.pausedReason == TrackingPausedReason.StillStandDetected) {
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
                            this.activity.recordingTime = it
                        },
                this.locationsChanged
                        .subscribeOn(Schedulers.computation())
                        .subscribe {
                            this.activity.addLocations(listOf(it))
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

        if (this.activity.userProfile != null) {
            val metActivityDefinition = MetDefinition.getMetDefinitionByCode(this.activity.activityCode)
            if (metActivityDefinition != null) {
                this.burnedEnergyChanged = this.recordingTimeChanged
                        .map {
                            it.seconds
                        }
                        .burnedEnergy(this.activity.userProfile!!.weightInKilograms,
                                this.activity.userProfile!!.heightInCentimeters,
                                this.activity.userProfile!!.age,
                                this.activity.userProfile!!.sex,
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
        get() = this.activity.startedAt

    public override fun resumeTracking() {
        if (this.isDestroyed) {
            throw ObjectDestroyedException()
        }

        if (this.stateChangedSubject.value!!.state == ActivitySessionState.Running) {
            throw IllegalStateException("Tracking must be paused first!")
        }

        this.resumeTrackingAndSetState(TrackingResumedReason.ManuallyResumed)
    }

    private fun changeState(newState: ActivitySessionState, pausedReason: TrackingPausedReason?) {
        this.stateChangedSubject.onNext(SessionStateInfo(newState, pausedReason))
    }

    public override fun pauseTracking() {
        if (this.isDestroyed) {
            throw ObjectDestroyedException()
        }

        if (this.stateChangedSubject.value!!.state != ActivitySessionState.Running) {
            throw IllegalStateException("Only possible in state \"${ActivitySessionState.Running}\"!")
        }

        this.pauseTrackingAndSetState(TrackingPausedReason.UserInitiated)
    }

    private fun resumeTrackingAndSetState(resumedReason: TrackingResumedReason) {
        if (!this.locationProvider.isActive) {
            this.locationProvider.startLocationUpdates()
        }

        this.recordingTimeTimer.start()

        this.activity.resume(resumedReason)

        this.changeState(ActivitySessionState.Running, null)
    }

    private fun pauseTrackingAndSetState(pausedReason: TrackingPausedReason, stopLocationProvider: Boolean = true) {
        if (this.stateChangedSubject.value!!.state == ActivitySessionState.Running) {
            if(this.locationsReceivedWithCurrentSegmentNumberCount.get() > 1) {
                this.currentSegmentNumber.incrementAndGet()
                this.locationsReceivedWithCurrentSegmentNumberCount.set(0)
            }

            if (this.locationProvider.isActive && stopLocationProvider) {
                this.locationProvider.stopLocationUpdates()
            }

            this.recordingTimeTimer.stop()

            this.activity.pause(pausedReason)
        }

        this.changeState(ActivitySessionState.Paused, pausedReason)
    }

    public override fun discardTracking() {
        if (this.isDestroyed) {
            throw ObjectDestroyedException()
        }

        if (this.stateChangedSubject.value!!.state != ActivitySessionState.Paused) {
            throw IllegalStateException("Only possible in state \"${ActivitySessionState.Paused}\"!")
        }

        this.destroy()
    }

    public override fun finishTracking(): Single<Activity> {
        if (this.isDestroyed) {
            throw ObjectDestroyedException()
        }

        if (this.stateChangedSubject.value!!.state != ActivitySessionState.Paused) {
            throw IllegalStateException("Only possible in state \"${ActivitySessionState.Paused}\"!")
        }

        val finishedActivity = this.activity

        return Single.create<Activity> {
            finishedActivity.finish()

            it.onSuccess(finishedActivity)
        }
        .flatMap {
            val activity = it

    this.activityService.saveActivity(activity)
            .flatMap {
                Single.just(activity)
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

        this.activityRecorderServiceNotification.destroy()

        this.recordingTimeTimer.destroy()

        this.subscriptions.dispose()

        this.stateChangedSubject.onComplete()

        this.isDestroyed = true

        this.service.currentSession = null
    }
}