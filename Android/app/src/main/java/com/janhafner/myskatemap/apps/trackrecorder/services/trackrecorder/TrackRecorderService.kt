package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity
import com.janhafner.myskatemap.apps.trackrecorder.ObservableTimer
import com.janhafner.myskatemap.apps.trackrecorder.getApplicationInjector
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.data.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.services.ITrackService
import com.janhafner.myskatemap.apps.trackrecorder.services.calories.BurnedEnergyCalculator
import com.janhafner.myskatemap.apps.trackrecorder.services.calories.IBurnedEnergyCalculator
import com.janhafner.myskatemap.apps.trackrecorder.services.calories.IMetActivityDefinitionFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.calories.NullBurnedEnergyCalculator
import com.janhafner.myskatemap.apps.trackrecorder.formatting.distance.IDistanceUnitFormatterFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.distance.DistanceCalculator
import com.janhafner.myskatemap.apps.trackrecorder.services.live.ILiveLocationTrackingServiceFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.live.ILiveLocationTrackingSession
import com.janhafner.myskatemap.apps.trackrecorder.services.stilldetection.StillDetectorBroadcastReceiver
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.notifications.TrackRecorderServiceNotification
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.notifications.TrackRecorderServiceNotificationChannel
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider.ILocationProviderFactory
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppConfig
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.settings.IUserProfile
import com.janhafner.myskatemap.apps.trackrecorder.statistics.TrackRecordingStatistic
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

internal final class TrackRecorderService : Service(), ITrackRecorderService {
    @Inject
    public lateinit var appSettings: IAppSettings

    @Inject
    public lateinit var appConfig: IAppConfig

    @Inject
    public lateinit var trackService: ITrackService

    @Inject
    public lateinit var distanceUnitFormatterFactory: IDistanceUnitFormatterFactory

    @Inject
    public lateinit var locationProviderFactory: ILocationProviderFactory

    @Inject
    public lateinit var metActivityDefinitionFactory: IMetActivityDefinitionFactory

    @Inject
    public lateinit var liveLocationTrackingServiceFactory: ILiveLocationTrackingServiceFactory

    @Inject
    public lateinit var locationAvailabilityChangedBroadcastReceiver: LocationAvailabilityChangedBroadcastReceiver

    @Inject
    public lateinit var notificationManager: NotificationManager

    @Inject
    public lateinit var stillDetectorBroadcastReceiver: StillDetectorBroadcastReceiver

    private lateinit var trackRecorderServiceNotificationChannel: TrackRecorderServiceNotificationChannel

    private lateinit var trackRecorderServiceNotification: TrackRecorderServiceNotification

    private var sessionSubscription: Disposable? = null

    public override fun onCreate() {
        this.getApplicationInjector().inject(this)

        // Starting from api level 26 a Notification channel needs to be created!
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.trackRecorderServiceNotificationChannel = TrackRecorderServiceNotificationChannel(notificationManager)
        }

        this.trackRecorderServiceNotification = TrackRecorderServiceNotification(this, this.distanceUnitFormatterFactory.createFormatter())

        super.onCreate()
    }

    public override var currentSession: ITrackRecordingSession? = null
        private set(value) {
            if(value != null && value != field) {
                this.sessionSubscription = value.sessionClosed.subscribe{
                    this.currentSession = null

                    this.terminateService()
                }
            }

            if(field != null && value == null) {
                this.sessionSubscription?.dispose()
                this.sessionSubscription = null
            }

            field = value

            this.hasCurrentSessionChangedSubject.onNext(value != null)
        }

    private val hasCurrentSessionChangedSubject: Subject<Boolean> = BehaviorSubject.createDefault(false)
    public override val hasCurrentSessionChanged: Observable<Boolean>
        get() = this.hasCurrentSessionChangedSubject

    public override fun useTrackRecording(trackRecording: TrackRecording): ITrackRecordingSession {
        if (this.currentSession != null) {
            throw IllegalStateException("Tracking already in progress!")
        }

        val locationProvider = this.locationProviderFactory.createLocationProvider(trackRecording.locationProviderTypeName)
        val observableTimer = ObservableTimer()
        val trackRecordingStatistic = TrackRecordingStatistic()
        val distanceCalculator = DistanceCalculator()
        val burnedEnergyCalculator: IBurnedEnergyCalculator
        val liveLocationTrackingSession: ILiveLocationTrackingSession

        if(trackRecording.fitnessActivity != null) {
            val metActivityDefinition = this.metActivityDefinitionFactory.getMetActivityDefinitionByCode(trackRecording.fitnessActivity!!.metActivityCode)
            if(metActivityDefinition != null) {
                burnedEnergyCalculator  = BurnedEnergyCalculator(trackRecording.fitnessActivity!!.weightInKilograms,
                        trackRecording.fitnessActivity!!.heightInCentimeters,
                        trackRecording.fitnessActivity!!.age,
                        trackRecording.fitnessActivity!!.sex,
                        metActivityDefinition.metValue)
            } else {
                burnedEnergyCalculator = NullBurnedEnergyCalculator()
            }
        } else {
            burnedEnergyCalculator = NullBurnedEnergyCalculator()
        }

        val liveLocationTrackingService = this.liveLocationTrackingServiceFactory.createService()
        liveLocationTrackingSession = liveLocationTrackingService.createSession()

        this.currentSession = TrackRecordingSession(
                this.appSettings,
                this.appConfig,
                this.trackService,
                this.trackRecorderServiceNotification,
                distanceCalculator,
                this.distanceUnitFormatterFactory,
                trackRecording,
                trackRecordingStatistic,
                burnedEnergyCalculator,
                locationProvider,
                observableTimer,
                liveLocationTrackingSession,
                this,
                this.stillDetectorBroadcastReceiver,
                this.locationAvailabilityChangedBroadcastReceiver)

        this.appSettings.currentTrackRecordingId = trackRecording.id

        return this.currentSession!!
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

        this.tryUpdateNotification()
    }

    public override fun onUnbind(intent: Intent?): Boolean {
        this.serviceUnbind()

        return true
    }

    private fun serviceUnbind() {
        this.trackRecorderServiceNotification.isBound = false

        this.tryUpdateNotification()
    }

    private fun tryUpdateNotification() {
        val notification = this.trackRecorderServiceNotification.update()
        if(notification != null) {
            this.startForeground(TrackRecorderServiceNotification.ID, notification)
        }
    }

    public override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intent != null) {
            when(intent.action) {
                TrackRecorderServiceNotification.ACTION_RESUME ->
                    this.currentSession!!.resumeTracking()
                TrackRecorderServiceNotification.ACTION_PAUSE ->
                    this.currentSession!!.pauseTracking()
                TrackRecorderServiceNotification.ACTION_TERMINATE -> {
                    this.terminateService()
                }
            }
        }

        return START_NOT_STICKY
    }

    private fun terminateService() {
        this.stopForeground(true)

        this.stopSelf()
    }

    private var isDestroyed: Boolean = false
    public override fun onDestroy() {
        if(this.isDestroyed) {
            return
        }

        this.currentSession?.destroy()

        this.hasCurrentSessionChangedSubject.onComplete()

        this.isDestroyed = true
    }
}

