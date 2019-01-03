package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session

import android.app.Service
import android.util.Log
import com.janhafner.myskatemap.apps.trackrecorder.BuildConfig
import com.janhafner.myskatemap.apps.trackrecorder.LocationAvailability
import com.janhafner.myskatemap.apps.trackrecorder.burnedenergy.IBurnedEnergyCalculator
import com.janhafner.myskatemap.apps.trackrecorder.burnedenergy.MetDefinition
import com.janhafner.myskatemap.apps.trackrecorder.burnedenergy.calculateBurnedEnergy
import com.janhafner.myskatemap.apps.trackrecorder.common.*
import com.janhafner.myskatemap.apps.trackrecorder.common.types.*
import com.janhafner.myskatemap.apps.trackrecorder.conversion.distance.IDistanceConverterFactory
import com.janhafner.myskatemap.apps.trackrecorder.distancecalculation.IDistanceCalculator
import com.janhafner.myskatemap.apps.trackrecorder.distancecalculation.calculateDistance
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.ILocationsAggregation
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.LocationsAggregation
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

    public override lateinit var locationsAggregation: ILocationsAggregation

    private val recordingTimeTimer: IObservableTimer = ObservableTimer()

    private val isStillObservableTimeout: IObservableTimeout

    public override lateinit var recordingTimeChanged: Observable<Period>
        private set

    public override lateinit var mapActivityStream: Observable<MapActivityStreamItem>
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
    public override val sessionClosed: Observable<ITrackRecordingSession> = this.sessionClosedSubject

    private val stateChangedSubject: BehaviorSubject<SessionStateInfo> = BehaviorSubject.createDefault(SessionStateInfo(TrackRecordingSessionState.Paused))
    public override val stateChanged: Observable<SessionStateInfo> = this.stateChangedSubject

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

        this.locationsAggregation = LocationsAggregation(this.locationsChanged)

        this.recordingTimeChanged = this.recordingTimeTimer.secondElapsed
        if (trackRecording.recordingTime != Period.ZERO && trackRecording.recordingTime.seconds > 0) {
            this.recordingTimeTimer.set(trackRecording.recordingTime.seconds)
        }

        this.distanceChanged = this.locationsChanged.map {
            listOf(it)
        }
        .calculateDistance(this.distanceCalculator)
        .replay(1)
        .autoConnect()

        val sessionActivityStream = trackRecording.locations.map {
            LocationReceivedActivityStreamItem(it.capturedAt, it.latitude, it.longitude) as MapActivityStreamItem
        }.toMutableList()
        sessionActivityStream.addAll(trackRecording.stateChanges.map {
            StartNewSegmentActivityStreamItem(it.at)
        })
        sessionActivityStream.sortBy {
            it.at
        }
        this.mapActivityStream = Observable.fromIterable(sessionActivityStream)
                .mergeWith(this.locationsChanged.map {
                    LocationReceivedActivityStreamItem(it.capturedAt, it.latitude, it.longitude)
                })
                .mergeWith(this.stateChanged.filter{
                    it.state == TrackRecordingSessionState.Paused && it.pausedReason != null
                }.map {
                    StartNewSegmentActivityStreamItem(DateTime.now())
                })
                .replay()
                .autoConnect()
        this.subscriptions.add(this.mapActivityStream.subscribe())

        this.isStillObservableTimeout = ObservableTimeout(BuildConfig.STILL_DETECTION_DETECTION_TIMEOUT_IN_MILLISECONDS.toLong())
        this.isStillChanged = this.locationsChanged
                .map {
                    false
                }
                .compose(TimeoutTransformer(this.isStillObservableTimeout) { true })

        /* TODO: ALG_#2
        this.isStillChanged = this.locationsChanged
                .map {
                    false
                }
                .mergeWith(this.locationsChanged
                        .debounce(BuildConfig.STILL_DETECTION_DETECTION_TIMEOUT_IN_MILLISECONDS.toLong(), TimeUnit.MILLISECONDS)
                        .filter {
                            this.currentState.state == TrackRecordingSessionState.Running
                        }
                        .map {
                            true
                        })
        */

        this.initializeSession()

        this.trackRecorderServiceNotification = TrackRecorderServiceNotification(this.service, this.appSettings, this, this.distanceConverterFactory)
    }

    private fun initializeSession() {
        this.subscriptions.addAll(
                this.isStillChanged
                        .subscribeOn(Schedulers.computation())
                        .subscribe {
                    if (it) {
                        Log.i("STILLDETECTION", "User does'nt move!")

                        if (this.currentState.state == TrackRecordingSessionState.Running) {
                            if (BuildConfig.STILL_DETECTION_ENABLE_PAUSERESUME) {
                                this.pauseTrackingAndSetState(TrackingPausedReason.StillStandDetected, false)

                                Log.i("STILLDETECTION", "Tracking was paused but location was kept on!")
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
                        .subscribeOn(Schedulers.computation())
                        .hasChanged()
                        .isNamed(IAppSettings::enableLiveLocation.name)
                        .startWith(PropertyChangedData(IAppSettings::enableLiveLocation.name, !this.appSettings.enableLiveLocation, this.appSettings.enableLiveLocation))
                        .flatMap {
                            if (this.appSettings.enableLiveLocation) {
                                this.liveSessionController.startSession()
                                        .onErrorReturn {  }
                                        .toObservable()
                            } else {
                                this.liveSessionController.endSession()
                                        .onErrorReturn {  }
                                        .toObservable()
                            }
                        }
                        .retry(3)
                        .subscribe(),
                this.appSettings.propertyChanged
                        .subscribeOn(Schedulers.computation())
                        .hasChanged()
                        .isNamed(IAppSettings::enableAutoPauseOnStill.name)
                        .subscribe {
                            if (this.appSettings.enableAutoPauseOnStill) {
                                this.tryStartAutoPauseTimeout()
                            } else {
                                this.isStillObservableTimeout.stop()
                            }
                        },
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
                            LiveLocation(it.capturedAt, it.latitude, it.longitude)
                        }
                        .buffer(30, TimeUnit.SECONDS, 20)
                        .filterNotEmpty()
                        .observeOn(Schedulers.io())
                        .flatMap {
                            this.liveSessionController.sendLocations(it)
                                    .subscribeOn(Schedulers.io())
                                    .onErrorReturn {  }
                                    .toObservable()
                        }
                        .subscribe(),
                this.stateChanged
                        .subscribeOn(Schedulers.computation())
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
        this.locationsAggregation.destroy()
        this.liveSessionController.endSession()
                .subscribeOn(Schedulers.io())
                .onErrorReturn {  }
                .subscribe()

        this.trackRecorderServiceNotification.destroy()

        this.recordingTimeTimer.destroy()
        this.locationProvider.destroy()

        this.subscriptions.dispose()

        this.stateChangedSubject.onComplete()

        this.isDestroyed = true

        this.sessionClosedSubject.onNext(this)
        this.sessionClosedSubject.onComplete()
    }
}