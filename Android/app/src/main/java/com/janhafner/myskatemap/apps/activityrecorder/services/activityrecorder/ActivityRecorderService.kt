package com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import com.janhafner.myskatemap.apps.activityrecorder.conversion.distance.IDistanceConverterFactory
import com.janhafner.myskatemap.apps.activityrecorder.core.getNotificationManager
import com.janhafner.myskatemap.apps.activityrecorder.core.types.Activity
import com.janhafner.myskatemap.apps.activityrecorder.getApplicationInjector
import com.janhafner.myskatemap.apps.activityrecorder.services.activity.IActivityService
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.notifications.ActivityRecorderServiceNotification
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.notifications.ActivityRecorderServiceNotificationChannel
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.provider.ILocationProvider
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.session.ActivitySession
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.session.IActivitySession
import com.janhafner.myskatemap.apps.activityrecorder.settings.IAppSettings
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

internal final class ActivityRecorderService : Service(), ITrackRecorderService {
    @Inject
    public lateinit var appSettings: IAppSettings

    @Inject
    public lateinit var distanceConverterFactory: IDistanceConverterFactory

    @Inject
    public lateinit var locationProvider: ILocationProvider

    @Inject
    public lateinit var liveSessionController: ILiveSessionController

    @Inject
    public lateinit var activityService: IActivityService

    private lateinit var activityRecorderServiceNotificationChannel: ActivityRecorderServiceNotificationChannel

    public override fun onCreate() {
        this.getApplicationInjector().inject(this)

        // Starting from api level 26 a Notification channel needs to be created!
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.activityRecorderServiceNotificationChannel = ActivityRecorderServiceNotificationChannel(this.getNotificationManager())
        }

        super.onCreate()
    }

    public override var currentSession: IActivitySession? = null
        set(value) {
            field = value

            this.hasCurrentSessionChangedSubject.onNext(value != null)
        }

    private val hasCurrentSessionChangedSubject: Subject<Boolean> = BehaviorSubject.createDefault(false)
    public override val hasCurrentSessionChanged: Observable<Boolean> = this.hasCurrentSessionChangedSubject

    public override fun useActivity(activity: Activity): IActivitySession {
        if (this.currentSession != null) {
            throw IllegalStateException("Activity already in progress!")
        }

        this.currentSession = ActivitySession(
                this.appSettings,
                this.distanceConverterFactory,
                activity,
                this.locationProvider,
                this,
                this.activityService,
                this.liveSessionController)

        return this.currentSession!!
    }

    public override fun onBind(intent: Intent?): IBinder {
        return ActivityRecorderServiceBinder(this)
    }

    public override fun onUnbind(intent: Intent?): Boolean {
        this.terminateService()

        return false
    }

    public override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intent != null) {
            when(intent.action) {
                ActivityRecorderServiceNotification.ACTION_RESUME ->
                    this.currentSession!!.resumeTracking()
                ActivityRecorderServiceNotification.ACTION_PAUSE ->
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

