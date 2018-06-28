package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder

import android.app.Service
import android.location.LocationManager
import android.os.Handler
import android.util.Log
import com.janhafner.myskatemap.apps.trackrecorder.IObservableTimer
import com.janhafner.myskatemap.apps.trackrecorder.Nothing
import com.janhafner.myskatemap.apps.trackrecorder.io.data.Location
import com.janhafner.myskatemap.apps.trackrecorder.io.data.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.services.ITrackService
import com.janhafner.myskatemap.apps.trackrecorder.services.calories.BurnedEnergy
import com.janhafner.myskatemap.apps.trackrecorder.services.calories.IBurnedEnergyCalculator
import com.janhafner.myskatemap.apps.trackrecorder.services.distance.ITrackDistanceCalculator
import com.janhafner.myskatemap.apps.trackrecorder.services.distance.ITrackDistanceUnitFormatterFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.live.ILiveLocationTrackingSession
import com.janhafner.myskatemap.apps.trackrecorder.services.stilldetection.ActivityRecognizerIntentService
import com.janhafner.myskatemap.apps.trackrecorder.services.stilldetection.StillDetectorBroadcastReceiver
import com.janhafner.myskatemap.apps.trackrecorder.services.temperature.IAmbientTemperatureService
import com.janhafner.myskatemap.apps.trackrecorder.services.temperature.Temperature
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.notifications.TrackRecorderServiceNotification
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
internal final class TrackRecordingSession(private val appSettings: IAppSettings,
                                           private val appConfig: IAppConfig,
                                           private val trackService: ITrackService,
                                           private val trackRecorderServiceNotification: TrackRecorderServiceNotification,
                                           private val trackDistanceCalculator: ITrackDistanceCalculator,
                                           private val trackDistanceUnitFormatterFactory: ITrackDistanceUnitFormatterFactory,
                                           private val trackRecording: TrackRecording,
                                           public override val statistic: ITrackRecordingStatistic,
                                           private val burnedEnergyCalculator: IBurnedEnergyCalculator,
                                           private val locationProvider: ILocationProvider,
                                           private val recordingTimeTimer: IObservableTimer,
                                           private val liveLocationTrackingSession: ILiveLocationTrackingSession,
                                           private val service: Service,
                                           private val stillDetectorBroadcastReceiver: StillDetectorBroadcastReceiver,
                                           private val locationAvailabilityChangedBroadcastReceiver: LocationAvailabilityChangedBroadcastReceiver,
                                           private val ambientTemperatureService: IAmbientTemperatureService) : ITrackRecordingSession {
    private val subscriptions: CompositeDisposable = CompositeDisposable()

    public override lateinit var recordingTimeChanged: Observable<Period>
        private set

    public override lateinit var locationsChanged: Observable<Location>
        private set

    public override val burnedEnergyChanged: Observable<BurnedEnergy> = this.burnedEnergyCalculator.calculatedValueChanged

    public override val trackDistanceChanged: Observable<Float>
        get() = this.trackDistanceCalculator.distanceCalculated

    private val sessionClosedSubject: Subject<ITrackRecordingSession> = PublishSubject.create()
    public override val sessionClosed: Observable<ITrackRecordingSession>
        get() = this.sessionClosedSubject

    private val stateChangedSubject: BehaviorSubject<TrackRecordingSessionState> = BehaviorSubject.createDefault(TrackRecordingSessionState.Paused)
    public override val stateChanged: Observable<TrackRecordingSessionState>
        get() = this.stateChangedSubject

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
                this.appSettings.appSettingsChanged.subscribe {
                    if (it.hasChanged && it.propertyName == "trackDistanceUnitFormatterTypeName") {
                        this.trackRecorderServiceNotification.trackDistanceUnitFormatter = this.trackDistanceUnitFormatterFactory.createTrackDistanceUnitFormatter()

                        this.tryUpdateNotification()
                    }
                },

                this.recordingTimeChanged
                        .subscribe {
                            this.burnedEnergyCalculator.calculate(it.seconds)
                        },

                this.recordingTimeChanged.subscribe { currentRecordingTime ->
                    this.trackRecording.recordingTime = currentRecordingTime

                    this.trackRecorderServiceNotification.recordingTime = currentRecordingTime
                },

                // TODO: Buffered; Less threads?
                this.locationsChanged
                        .buffer(1, TimeUnit.SECONDS)
                        .filter {
                            it.any()
                        }
                        .subscribe {
                            this.statistic.addAll(it)

                            this.trackRecording.locations.putAll(it.map {
                                Pair(it.sequenceNumber, it)
                            })

                            this.trackDistanceCalculator.addAll(it)

                            this.liveLocationTrackingSession.sendLocations(it.map {
                                it.toSimpleLocation()
                            })
                        },

                // TODO: Unbuffered; Less threads?
                /*this.locationsChanged
                        .subscribe {
                            this.statistic.add(it)

                            this.trackRecording.locations.put(it.sequenceNumber, it)

                            this.trackDistanceCalculator.add(it)

                            this.liveLocationTrackingSession.sendLocations(listOf(it.toSimpleLocation()))
                        },*/

                // TODO: More threads?
                /*this.locationsChanged
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
                        },*/

                this.stateChanged.subscribe {
                    this.trackRecorderServiceNotification.state = it

                    this.tryUpdateNotification()
                },

                this.trackDistanceChanged.subscribe {
                    this.trackRecorderServiceNotification.trackDistance = it

                    this.tryUpdateNotification()
                })

        this.initializeLocationAvailabilityChangedBroadcastReceiver()
        this.initializeStillDetectorBroadcastReceiver()
        this.initializeAmbientTemperatureService()
    }

    private fun initializeLocationAvailabilityChangedBroadcastReceiver() {
        this.service.registerReceiver(this.locationAvailabilityChangedBroadcastReceiver, android.content.IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION))

        this.subscriptions.add(this.locationAvailabilityChangedBroadcastReceiver.locationAvailabilityChanged.subscribe{
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

        this.subscriptions.add(this.stillDetectorBroadcastReceiver.startDetection(1500).subscribe{
            if(it) {
                if(this.stateChangedSubject.value == TrackRecordingSessionState.Running) {
                    // this.pauseTrackingAndSetState(TrackRecordingSessionState.Paused)
                    Log.i("TRS", "StillDetector: tracking would be resumed!")
                }
            } else {
                if(this.stateChangedSubject.value == TrackRecordingSessionState.Paused) {
                    //this.resumeTracking()
                    Log.i("TRS", "StillDetector: tracking would be paused!")
                }
            }
        })
    }

    private fun initializeAmbientTemperatureService() {
        this.subscriptions.add(
                this.ambientTemperatureService.startListening().subscribe {
                    this.statistic.addAmbientTemperature(it)
                }
        )
    }

    private fun tryUpdateNotification() {
        val notification = this.trackRecorderServiceNotification.update()
        if(notification != null) {
            this.service.startForeground(TrackRecorderServiceNotification.ID, notification)
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

    private fun pauseTrackingAndSetState(state: TrackRecordingSessionState) {
        if(this.stateChangedSubject.value == TrackRecordingSessionState.Running) {
            this.locationProvider.stopLocationUpdates()
            this.recordingTimeTimer.stop()

            this.trackRecording.pause()
        }

        Handler().postDelayed({
            // Hack: Because processing of locationsReceived is buffered every second,
            // it can happen that by the time the tracking is saved the list of locationsReceived is still being updated.
            // To prevent exceptions, delay saving 1 second and hope updates are done; until I find a better solution!
            this.saveTracking()
        }, 1000)

        this.changeState(state)
    }

    public override fun resumeTracking() {
        if(this.isDestroyed) {
            throw IllegalStateException("Object is destroyed!")
        }

        if(this.stateChangedSubject.value == TrackRecordingSessionState.Running) {
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
        if(this.isDestroyed) {
            throw IllegalStateException("Object is destroyed!")
        }

        if(this.stateChangedSubject.value != TrackRecordingSessionState.Running) {
            throw IllegalStateException("Only possible in state \"Running\"!")
        }

        this.pauseTrackingAndSetState(TrackRecordingSessionState.Paused)
    }

    public override fun saveTracking() {
        if(this.isDestroyed) {
            throw IllegalStateException("Object is destroyed!")
        }

        if(this.stateChangedSubject.value == TrackRecordingSessionState.Running) {
            throw IllegalStateException("Only possible in state \"Paused\" or \"LocationServicesUnavailable\"!")
        }

        this.trackService.save(this.trackRecording)

        this.recordingSavedSubject.onNext(Nothing.instance)
    }

    public override fun discardTracking() {
        if(this.isDestroyed) {
            throw IllegalStateException("Object is destroyed!")
        }

        if(this.stateChangedSubject.value != TrackRecordingSessionState.Paused
            && this.stateChangedSubject.value != TrackRecordingSessionState.LocationServicesUnavailable) {
            throw IllegalStateException("Only possible in state \"Paused\" or \"LocationServicesUnavailable\"!")
        }

        this.recordingTimeTimer.reset()
        this.locationProvider.resetSequenceNumber()

        this.trackDistanceCalculator.clear()

        this.trackService.delete(this.trackRecording.id.toString())

        this.destroy()
    }

    public override fun finishTracking(): TrackRecording {
        if(this.isDestroyed) {
            throw IllegalStateException("Object is destroyed!")
        }

        if(this.stateChangedSubject.value != TrackRecordingSessionState.Paused) {
            throw IllegalStateException("Only possible in state \"Paused\"!")
        }

        val finishedTrackRecording = this.trackRecording

        finishedTrackRecording.finish()
        this.saveTracking()

        this.recordingTimeTimer.reset()
        this.locationProvider.resetSequenceNumber()

        this.trackDistanceCalculator.clear()

        this.destroy()

        return finishedTrackRecording
    }

    private var isDestroyed: Boolean = false
    public override fun destroy() {
        if(this.isDestroyed) {
            return
        }

        this.stillDetectorBroadcastReceiver.stopDetection()

        this.service.unregisterReceiver(this.stillDetectorBroadcastReceiver)
        this.service.unregisterReceiver(this.locationAvailabilityChangedBroadcastReceiver)
        this.ambientTemperatureService.stopListening()

        this.burnedEnergyCalculator.destroy()
        this.recordingTimeTimer.destroy()
        this.trackDistanceCalculator.destroy()
        this.locationProvider.destroy()
        this.liveLocationTrackingSession.endSession()

        this.subscriptions.dispose()

        this.recordingSavedSubject.onComplete()
        this.stateChangedSubject.onComplete()

        this.isDestroyed = true

        this.sessionClosedSubject.onNext(this)
        this.sessionClosedSubject.onComplete()
    }
}