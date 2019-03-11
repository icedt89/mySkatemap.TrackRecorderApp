package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import com.janhafner.myskatemap.apps.trackrecorder.core.getNotificationManager
import com.janhafner.myskatemap.apps.trackrecorder.core.types.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.conversion.distance.IDistanceConverterFactory
import com.janhafner.myskatemap.apps.trackrecorder.getApplicationInjector
import com.janhafner.myskatemap.apps.trackrecorder.services.track.ITrackService
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.notifications.TrackRecorderServiceNotification
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.notifications.TrackRecorderServiceNotificationChannel
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider.ILocationProvider
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session.ITrackRecordingSession
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session.TrackRecordingSession
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import io.reactivex.Observable
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
    public lateinit var liveSessionController: ILiveSessionController

    @Inject
    public lateinit var trackService: ITrackService

    private lateinit var trackRecorderServiceNotificationChannel: TrackRecorderServiceNotificationChannel

    public override fun onCreate() {
        this.getApplicationInjector().inject(this)

        // Starting from api level 26 a Notification channel needs to be created!
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.trackRecorderServiceNotificationChannel = TrackRecorderServiceNotificationChannel(this.getNotificationManager())
        }

        super.onCreate()
    }

    public override var currentSession: ITrackRecordingSession? = null
        set(value) {
            field = value

            this.hasCurrentSessionChangedSubject.onNext(value != null)
        }

    private val hasCurrentSessionChangedSubject: Subject<Boolean> = BehaviorSubject.createDefault(false)
    public override val hasCurrentSessionChanged: Observable<Boolean> = this.hasCurrentSessionChangedSubject

    public override fun useTrackRecording(trackRecording: TrackRecording): ITrackRecordingSession {
        if (this.currentSession != null) {
            throw IllegalStateException("Tracking already in progress!")
        }

        this.currentSession = TrackRecordingSession(
                this.appSettings,
                this.distanceConverterFactory,
                trackRecording,
                this.locationProvider,
                this,
                this.trackService,
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
        this.locationProvider.destroy()

        this.hasCurrentSessionChangedSubject.onComplete()

        this.isDestroyed = true
    }
}

