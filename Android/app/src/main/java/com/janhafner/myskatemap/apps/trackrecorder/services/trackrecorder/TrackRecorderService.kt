package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import com.janhafner.myskatemap.apps.trackrecorder.burnedenergy.IBurnedEnergyCalculator
import com.janhafner.myskatemap.apps.trackrecorder.common.getNotificationManager
import com.janhafner.myskatemap.apps.trackrecorder.common.types.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.conversion.distance.IDistanceConverterFactory
import com.janhafner.myskatemap.apps.trackrecorder.distancecalculation.IDistanceCalculator
import com.janhafner.myskatemap.apps.trackrecorder.getApplicationInjector
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.notifications.TrackRecorderServiceNotification
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.notifications.TrackRecorderServiceNotificationChannel
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider.ILocationProvider
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session.ITrackRecordingSession
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session.TrackRecordingSession
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
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
    public lateinit var distanceCalculator: IDistanceCalculator

    @Inject
    public lateinit var burnedEnergyCalculator: IBurnedEnergyCalculator

    @Inject
    public lateinit var liveSessionController: ILiveSessionController

    private lateinit var trackRecorderServiceNotificationChannel: TrackRecorderServiceNotificationChannel

    private var sessionSubscription: Disposable? = null

    public override fun onCreate() {
        this.getApplicationInjector().inject(this)

        // Starting from api level 26 a Notification channel needs to be created!
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.trackRecorderServiceNotificationChannel = TrackRecorderServiceNotificationChannel(this.getNotificationManager())
        }

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

        this.currentSession = TrackRecordingSession(
                this.appSettings,
                this.distanceCalculator,
                this.distanceConverterFactory,
                trackRecording,
                this.burnedEnergyCalculator,
                this.locationProvider,
                this,
                this.liveSessionController)

        return this.currentSession!!
    }

    public override fun onBind(intent: Intent?): IBinder {
        return TrackRecorderServiceBinder(this)
    }

    public override fun onUnbind(intent: Intent?): Boolean {
        this.terminateService()

        return false
    }

    public override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intent != null) {
            when(intent.action) {
                TrackRecorderServiceNotification.ACTION_RESUME ->
                    this.currentSession!!.resumeTracking()
                TrackRecorderServiceNotification.ACTION_PAUSE ->
                    this.currentSession!!.pauseTracking()
                TrackRecorderServiceNotification.ACTION_FINISH ->
                    this.currentSession!!.finishTracking()
                TrackRecorderServiceNotification.ACTION_DISCARD ->
                    this.currentSession!!.discardTracking()
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

