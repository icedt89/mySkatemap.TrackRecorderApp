package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import com.janhafner.myskatemap.apps.trackrecorder.aggregations.LocationsAggregation
import com.janhafner.myskatemap.apps.trackrecorder.common.ObservableTimer
import com.janhafner.myskatemap.apps.trackrecorder.getApplicationInjector
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance.IDistanceConverterFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.burnedenergy.BurnedEnergyCalculator
import com.janhafner.myskatemap.apps.trackrecorder.services.burnedenergy.IBurnedEnergyCalculator
import com.janhafner.myskatemap.apps.trackrecorder.services.burnedenergy.IMetActivityDefinitionFactory
import com.janhafner.myskatemap.apps.trackrecorder.services.burnedenergy.NullBurnedEnergyCalculator
import com.janhafner.myskatemap.apps.trackrecorder.services.distance.DistanceCalculator
import com.janhafner.myskatemap.apps.trackrecorder.services.locationavailability.LocationAvailabilityChangedBroadcastReceiver
import com.janhafner.myskatemap.apps.trackrecorder.services.models.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.notifications.TrackRecorderServiceNotification
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.notifications.TrackRecorderServiceNotificationChannel
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider.ILocationProvider
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session.ITrackRecordingSession
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session.TrackRecordingSession
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.stilldetection.IStillDetector
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

internal final class TrackRecorderService : Service(), ITrackRecorderService {
    @Inject
    public lateinit var appSettings: IAppSettings

    @Inject
    public lateinit var distanceConverterFactory: IDistanceConverterFactory

    @Inject
    public lateinit var locationProvider: ILocationProvider

    @Inject
    public lateinit var metActivityDefinitionFactory: IMetActivityDefinitionFactory

    @Inject
    public lateinit var locationAvailabilityChangedBroadcastReceiver: LocationAvailabilityChangedBroadcastReceiver

    @Inject
    public lateinit var stillDetector: IStillDetector

    @Inject
    public lateinit var notificationManager: NotificationManager

    private lateinit var trackRecorderServiceNotificationChannel: TrackRecorderServiceNotificationChannel

    private lateinit var trackRecorderServiceNotification: TrackRecorderServiceNotification

    private var sessionSubscription: Disposable? = null

    public override fun onCreate() {
        this.getApplicationInjector().inject(this)

        // Starting from api level 26 a Notification channel needs to be created!
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.trackRecorderServiceNotificationChannel = TrackRecorderServiceNotificationChannel(notificationManager)
        }

        this.trackRecorderServiceNotification = TrackRecorderServiceNotification(this, this.distanceConverterFactory.createConverter())
        this.trackRecorderServiceNotification.vibrateOnLocationAvailabilityLoss = this.appSettings.vibrateOnLocationAvailabilityLoss

        this.registerReceiver(this.locationAvailabilityChangedBroadcastReceiver, IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION))

        super.onCreate()
    }

    public override var currentSession: ITrackRecordingSession? = null
        private set(value) {
            if(value != null && value != field) {
                this.sessionSubscription = value.sessionClosed
                        .subscribe {
                            this.currentSession = null
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
    public override val hasCurrentSessionChanged: Observable<Boolean> = this.hasCurrentSessionChangedSubject.subscribeOn(Schedulers.computation())

    public override fun useTrackRecording(trackRecording: TrackRecording): ITrackRecordingSession {
        if (this.currentSession != null) {
            throw IllegalStateException("Tracking already in progress!")
        }

        val observableTimer = ObservableTimer()
        val trackRecordingStatistic = LocationsAggregation()
        val distanceCalculator = DistanceCalculator()
        val burnedEnergyCalculator: IBurnedEnergyCalculator

        if(trackRecording.userProfile != null) {
            val metActivityDefinition = this.metActivityDefinitionFactory.getMetActivityDefinitionByCode(trackRecording.userProfile!!.metActivityCode)
            if(metActivityDefinition != null) {
                burnedEnergyCalculator  = BurnedEnergyCalculator(trackRecording.userProfile!!.weightInKilograms,
                        trackRecording.userProfile!!.heightInCentimeters,
                        trackRecording.userProfile!!.age,
                        trackRecording.userProfile!!.sex,
                        metActivityDefinition.metValue)
            } else {
                burnedEnergyCalculator = NullBurnedEnergyCalculator()
            }
        } else {
            burnedEnergyCalculator = NullBurnedEnergyCalculator()
        }

        this.currentSession = TrackRecordingSession(
                this.appSettings,
                this.trackRecorderServiceNotification,
                distanceCalculator,
                this.distanceConverterFactory,
                trackRecording,
                trackRecordingStatistic,
                burnedEnergyCalculator,
                this.locationProvider,
                observableTimer,
                this,
                this.stillDetector,
                this.locationAvailabilityChangedBroadcastReceiver)

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
        this.tryUpdateNotification()
    }

    public override fun onUnbind(intent: Intent?): Boolean {
        this.serviceUnbind()

        this.terminateService()

        return false
    }

    private fun serviceUnbind() {
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
            }
        }

        // return START_STICKY
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

        this.unregisterReceiver(this.locationAvailabilityChangedBroadcastReceiver)

        this.hasCurrentSessionChangedSubject.onComplete()

        this.isDestroyed = true
    }
}

