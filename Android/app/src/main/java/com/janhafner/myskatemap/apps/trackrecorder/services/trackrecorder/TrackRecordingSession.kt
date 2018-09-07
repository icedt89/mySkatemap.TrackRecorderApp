package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder

import android.app.Service
import android.location.LocationManager
import com.janhafner.myskatemap.apps.trackrecorder.*
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.data.Location
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.data.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.services.ITrackService
import com.janhafner.myskatemap.apps.trackrecorder.services.burnedenergy.BurnedEnergy
import com.janhafner.myskatemap.apps.trackrecorder.services.burnedenergy.IBurnedEnergyCalculator
import com.janhafner.myskatemap.apps.trackrecorder.services.distance.IDistanceCalculator
import com.janhafner.myskatemap.apps.trackrecorder.formatting.distance.IDistanceUnitFormatterFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.live.ILiveLocationTrackingSession
import com.janhafner.myskatemap.apps.trackrecorder.services.stilldetection.ActivityRecognizerIntentService
import com.janhafner.myskatemap.apps.trackrecorder.services.stilldetection.StillDetectorBroadcastReceiver
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.notifications.TrackRecorderServiceNotification
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider.ILocationProvider
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.statistics.ITrackRecordingStatistic
import io.reactivex.Observable
import io.reactivex.Single
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
                                           private val distanceUnitFormatterFactory: IDistanceUnitFormatterFactory,
                                           private val trackRecording: TrackRecording,
                                           public override val statistic: ITrackRecordingStatistic,
                                           private val burnedEnergyCalculator: IBurnedEnergyCalculator,
                                           private val locationProvider: ILocationProvider,
                                           private val recordingTimeTimer: IObservableTimer,
                                           private val liveLocationTrackingSession: ILiveLocationTrackingSession,
                                           private val service: Service,
                                           private val stillDetectorBroadcastReceiver: StillDetectorBroadcastReceiver,
                                           private val locationAvailabilityChangedBroadcastReceiver: LocationAvailabilityChangedBroadcastReceiver) : ITrackRecordingSession {
    private val subscriptions: CompositeDisposable = CompositeDisposable()

    public override lateinit var recordingTimeChanged: Observable<Period>
        private set

    public override lateinit var locationsChanged: Observable<Location>
        private set

    public override val burnedEnergyChanged: Observable<BurnedEnergy> = this.burnedEnergyCalculator.calculatedValueChanged

    public override val distanceChanged: Observable<Float> = this.distanceCalculator.distanceCalculated

    private val sessionClosedSubject: Subject<ITrackRecordingSession> = PublishSubject.create()
    public override val sessionClosed: Observable<ITrackRecordingSession> = this.sessionClosedSubject.subscribeOn(Schedulers.computation())

    private val stateChangedSubject: BehaviorSubject<TrackRecordingSessionState> = BehaviorSubject.createDefault(TrackRecordingSessionState.Paused)
    public override val stateChanged: Observable<TrackRecordingSessionState> = this.stateChangedSubject.subscribeOn(Schedulers.computation())

    init {
        this.locationsChanged = this.locationProvider.locationsReceived.replay().autoConnect()
        if (trackRecording.locations.any()) {
            val sortedLocations = trackRecording.locations.toSortedMap().values

            this.locationProvider.overrideSequenceNumber(sortedLocations.last().sequenceNumber)

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
                        .subscribe {
                            if (it.hasChanged && it.propertyName == IAppSettings::distanceUnitFormatterTypeName.name) {
                                this.trackRecorderServiceNotification.distanceUnitFormatter = this.distanceUnitFormatterFactory.createFormatter()

                                this.tryUpdateNotification()
                            }
                        },
                this.recordingTimeChanged
                        .subscribe {
                            this.burnedEnergyCalculator.calculate(it.seconds)
                        },
                this.recordingTimeChanged
                        .subscribe {
                            currentRecordingTime ->
                                this.trackRecording.recordingTime = currentRecordingTime

                                this.trackRecorderServiceNotification.recordingTime = currentRecordingTime
                        },
                this.locationsChanged
                        .buffer(1, TimeUnit.SECONDS)
                        .filterNotEmpty()
                        .subscribe {
                            this.statistic.addAll(it)

                            this.trackRecording.locations.putAll(it.map {
                                Pair(it.sequenceNumber, it)
                            })

                            this.distanceCalculator.addAll(it)
                        },
                this.locationsChanged
                        .map {
                            it.toSimpleLocation()
                        }
                        .buffer(5, TimeUnit.SECONDS)
                        .filter {
                            it.any()
                        }
                        // This could also without any problems be executed on the computation-scheduler.
                        // But there will be network IO involved so we switch to the io-scheduler!
                        .observeOn(Schedulers.io())
                        .subscribe {
                            this.liveLocationTrackingSession.sendLocations(it)
                        },
                this.stateChanged
                        .subscribe {
                            this.trackRecorderServiceNotification.state = it

                            this.tryUpdateNotification()
                        },
                this.distanceChanged
                        .subscribe {
                            this.trackRecorderServiceNotification.distance = it

                            this.tryUpdateNotification()
                        })

        this.initializeLocationAvailabilityChangedBroadcastReceiver()
        this.initializeStillDetectorBroadcastReceiver()
    }

    private fun initializeLocationAvailabilityChangedBroadcastReceiver() {
        this.service.registerReceiver(this.locationAvailabilityChangedBroadcastReceiver, android.content.IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION))

        this.subscriptions.add(this.locationAvailabilityChangedBroadcastReceiver.locationAvailabilityChanged
                .subscribe {
                    if (!it) {
                        if (this.stateChangedSubject.value == TrackRecordingSessionState.Running
                                || this.stateChangedSubject.value == TrackRecordingSessionState.Paused) {
                            this.pauseTrackingAndSetState(TrackRecordingSessionState.LocationServicesUnavailable)
                        }
                    } else {
                        if (this.stateChangedSubject.value == TrackRecordingSessionState.LocationServicesUnavailable) {
                            this.resumeTracking()
                        }
                    }
                })
    }

    private fun initializeStillDetectorBroadcastReceiver() {
        this.service.registerReceiver(this.stillDetectorBroadcastReceiver, android.content.IntentFilter(ActivityRecognizerIntentService.INTENT_ACTION_NAME))

        this.subscriptions.add(this.stillDetectorBroadcastReceiver.startDetection(BuildConfig.TRACKING_STILLDETECTOR_DETECTION_INTERVAL_IN_MILLISECONDS.toLong())
                .subscribe {
                    if (it) {
                        if (this.stateChangedSubject.value == TrackRecordingSessionState.Running) {
                            this.pauseTrackingAndSetState(TrackRecordingSessionState.Paused)
                        }
                    } else {
                        if (this.stateChangedSubject.value == TrackRecordingSessionState.Paused) {
                            this.resumeTracking()
                        }
                    }
                })
    }

    private fun tryUpdateNotification() {
        val notification = this.trackRecorderServiceNotification.update()
        if (notification != null) {
            this.service.startForeground(TrackRecorderServiceNotification.ID, notification)
        }
    }

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

    public override fun resumeTracking() {
        if (this.isDestroyed) {
            throw IllegalStateException("Object is destroyed!")
        }

        if (this.stateChangedSubject.value == TrackRecordingSessionState.Running) {
            throw IllegalStateException("Tracking must be paused first!")
        }

        this.locationProvider.startLocationUpdates()
        this.recordingTimeTimer.start()

        this.trackRecording.resume()

        this.changeState(TrackRecordingSessionState.Running)
    }

    private fun changeState(newState: TrackRecordingSessionState) {
        this.stateChangedSubject.onNext(newState)
    }

    public override fun pauseTracking() {
        if (this.isDestroyed) {
            throw IllegalStateException("Object is destroyed!")
        }

        if (this.stateChangedSubject.value != TrackRecordingSessionState.Running) {
            throw IllegalStateException("Only possible in state \"${TrackRecordingSessionState.Running}\"!")
        }

        this.pauseTrackingAndSetState(TrackRecordingSessionState.Paused)
    }

    private fun pauseTrackingAndSetState(state: TrackRecordingSessionState) {
        if (this.stateChangedSubject.value == TrackRecordingSessionState.Running) {
            this.locationProvider.stopLocationUpdates()
            this.recordingTimeTimer.stop()

            this.trackRecording.pause()
        }

        this.changeState(state)
    }

    public override fun discardTracking() {
        if (this.isDestroyed) {
            throw IllegalStateException("Object is destroyed!")
        }

        if (this.stateChangedSubject.value != TrackRecordingSessionState.Paused
                && this.stateChangedSubject.value != TrackRecordingSessionState.LocationServicesUnavailable) {
            throw IllegalStateException("Only possible in state \"${TrackRecordingSessionState.Paused}\" or \"${TrackRecordingSessionState.LocationServicesUnavailable}\"!")
        }

        this.recordingTimeTimer.reset()
        this.locationProvider.resetSequenceNumber()

        this.distanceCalculator.clear()

        this.destroy()
    }

    public override fun finishTracking(): TrackRecording {
        if (this.isDestroyed) {
            throw IllegalStateException("Object is destroyed!")
        }

        if (this.stateChangedSubject.value != TrackRecordingSessionState.Paused) {
            throw IllegalStateException("Only possible in state \"${TrackRecordingSessionState.Paused}\"!")
        }

        val finishedTrackRecording = this.trackRecording

        finishedTrackRecording.finish()

        this.recordingTimeTimer.reset()
        this.locationProvider.resetSequenceNumber()

        this.distanceCalculator.clear()

        this.destroy()

        return finishedTrackRecording
    }

    private var isDestroyed: Boolean = false
    public override fun destroy() {
        if (this.isDestroyed) {
            return
        }

        this.stillDetectorBroadcastReceiver.stopDetection()

        this.service.unregisterReceiver(this.stillDetectorBroadcastReceiver)
        this.service.unregisterReceiver(this.locationAvailabilityChangedBroadcastReceiver)

        this.statistic.destroy()
        this.recordingTimeTimer.destroy()
        this.burnedEnergyCalculator.destroy()
        this.distanceCalculator.destroy()
        this.locationProvider.destroy()
        this.liveLocationTrackingSession.endSession()

        this.subscriptions.dispose()

        this.stateChangedSubject.onComplete()

        this.isDestroyed = true

        this.sessionClosedSubject.onNext(this)
        this.sessionClosedSubject.onComplete()
    }
}