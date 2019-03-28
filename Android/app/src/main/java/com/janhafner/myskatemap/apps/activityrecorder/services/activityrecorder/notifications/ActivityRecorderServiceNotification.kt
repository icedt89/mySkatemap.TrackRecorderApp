package com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.notifications

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.janhafner.myskatemap.apps.activityrecorder.R
import com.janhafner.myskatemap.apps.activityrecorder.core.IDestroyable
import com.janhafner.myskatemap.apps.activityrecorder.core.formatRecordingTime
import com.janhafner.myskatemap.apps.activityrecorder.core.hasChanged
import com.janhafner.myskatemap.apps.activityrecorder.core.isNamed
import com.janhafner.myskatemap.apps.activityrecorder.core.types.TrackingPausedReason
import com.janhafner.myskatemap.apps.activityrecorder.conversion.distance.IDistanceConverter
import com.janhafner.myskatemap.apps.activityrecorder.conversion.distance.IDistanceConverterFactory
import com.janhafner.myskatemap.apps.activityrecorder.infrastructure.distance.format
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.ActivityRecorderService
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.session.IActivitySession
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.session.SessionStateInfo
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.session.ActivitySessionState
import com.janhafner.myskatemap.apps.activityrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityrecorder.ActivityRecorderActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.joda.time.Period


internal final class ActivityRecorderServiceNotification(private val service: Service,
                                                         private val appSettings: IAppSettings,
                                                         activitySession: IActivitySession,
                                                         private val distanceConverterFactory: IDistanceConverterFactory) : IDestroyable {
    private val subscriptions: CompositeDisposable = CompositeDisposable()

    private var distanceConverter: IDistanceConverter = this.distanceConverterFactory.createConverter()

    private var state: SessionStateInfo = SessionStateInfo(ActivitySessionState.Paused)

    private var recordingTime: Period? = null

    private var distance: Float? = null

    private var vibrateOnLocationAvailabilityLoss: Boolean = false

    init {
        this.subscriptions.addAll(
                appSettings.propertyChanged
                        .subscribeOn(Schedulers.computation())
                        .hasChanged()
                        .isNamed(IAppSettings::vibrateOnLocationAvailabilityLoss.name)
                        .subscribe{
                            this.vibrateOnLocationAvailabilityLoss = this.appSettings.vibrateOnLocationAvailabilityLoss

                            this.update()
                        }
        )

        this.subscribeToSession(activitySession)
    }

    private fun createDefaultNotificationBuilder(): NotificationCompat.Builder {
        val notificationCompatBuilder: NotificationCompat.Builder
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationCompatBuilder = NotificationCompat.Builder(this.service, ActivityRecorderServiceNotificationChannel.ID)
        } else {
            @Suppress("DEPRECATION")
            notificationCompatBuilder = NotificationCompat.Builder(this.service)
        }

        notificationCompatBuilder.setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
        notificationCompatBuilder.setSmallIcon(R.drawable.ic_stat_track_recorder)
        notificationCompatBuilder.setShowWhen(false)
        notificationCompatBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        notificationCompatBuilder.setOngoing(true)

        val intent = Intent(this.service, ActivityRecorderActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)

        val pendingIntent = PendingIntent.getActivity(this.service, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        notificationCompatBuilder.setContentIntent(pendingIntent)

        return notificationCompatBuilder
    }

    private fun subscribeToSession(trackRecorderSession: IActivitySession) {
        this.subscriptions.addAll(
                trackRecorderSession.recordingTimeChanged
                        .doOnNext {
                            this.recordingTime = it
                        }
                        .map {
                            Unit
                        }
                        .mergeWith(trackRecorderSession.stateChanged
                                .doOnNext{
                                    this.state = it
                                }
                                .map {
                                    Unit
                                })
                        .mergeWith(trackRecorderSession.distanceChanged
                                .doOnNext {
                                    this.distance = it
                                }
                                .map {
                                    Unit
                                })
                        .subscribeOn(Schedulers.computation())
                        .subscribe {
                            this.update()
                        }
        )
    }

    private fun update() {
        val notificationCompatBuilder = this.createDefaultNotificationBuilder()
        notificationCompatBuilder.setVibrate(null)

        when(this.state.state) {
            ActivitySessionState.Running ->
                notificationCompatBuilder.setContentTitle(this.service.getString(R.string.activityrecorderservice_notification_status_running))
            ActivitySessionState.Paused ->
                if(this.state.pausedReason == TrackingPausedReason.LocationServicesUnavailable) {
                    notificationCompatBuilder.setContentTitle(this.service.getString(R.string.activityrecorderservice_notification_status_locationservicesunavailable_paused))

                    if(this.vibrateOnLocationAvailabilityLoss) {
                        notificationCompatBuilder.setVibrate(longArrayOf(500, 500, 500, 500, 500, 500))
                    }

                } else if(this.state.pausedReason == TrackingPausedReason.StillStandDetected) {
                    notificationCompatBuilder.setContentTitle(this.service.getString(R.string.activityrecorderservice_notification_status_stillstand_paused))
                } else {
                    notificationCompatBuilder.setContentTitle(this.service.getString(R.string.activityrecorderservice_notification_status_paused))
                }
        }

        val contentText = this.buildContentText()
        notificationCompatBuilder.setContentText(contentText)

        notificationCompatBuilder.mActions.clear()

        if (this.state.state == ActivitySessionState.Paused) {
            if(this.state.pausedReason != TrackingPausedReason.LocationServicesUnavailable) {
                notificationCompatBuilder.addAction(NotificationCompat.Action.Builder(R.drawable.ic_play_arrow_24dp, this.service.getString(R.string.activityrecorderservice_notification_action_resume), PendingIntent.getService(this.service, 0, Intent(ACTION_RESUME, null, this.service, ActivityRecorderService::class.java), PendingIntent.FLAG_UPDATE_CURRENT)).build())
            }

            notificationCompatBuilder.addAction(NotificationCompat.Action.Builder(R.drawable.ic_flag_24dp, this.service.getString(R.string.activityrecorderservice_notification_action_finish), PendingIntent.getActivity(this.service, 0, Intent(ACTION_FINISH, null, this.service, ActivityRecorderActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT)).build())
            notificationCompatBuilder.addAction(NotificationCompat.Action.Builder(R.drawable.ic_clear_24dp, this.service.getString(R.string.activityrecorderservice_notification_action_discard), PendingIntent.getActivity(this.service, 0, Intent(ACTION_DISCARD, null, this.service, ActivityRecorderActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT)).build())
        } else if (this.state.state == ActivitySessionState.Running) {
            notificationCompatBuilder.addAction(NotificationCompat.Action.Builder(R.drawable.ic_pause_24dp, this.service.getString(R.string.activityrecorderservice_notification_action_pause), PendingIntent.getService(this.service, 0, Intent(ACTION_PAUSE, null, this.service, ActivityRecorderService::class.java), PendingIntent.FLAG_UPDATE_CURRENT)).build())
        }

        val notification = notificationCompatBuilder.build()
        this.service.startForeground(ActivityRecorderServiceNotification.ID, notification)
    }

    private fun buildContentText(): String {
        var displayedDistance = 0.0f
        if(this.distance != null) {
            displayedDistance = this.distance!!
        }

        var displayedRecordingTime = Period.ZERO
        if(this.recordingTime != null) {
            displayedRecordingTime = this.recordingTime
        }

        if(displayedDistance == 0.0f && displayedRecordingTime == Period.ZERO) {
            return ""
        }

        val recordingTimeDisplayTemplate = displayedRecordingTime.formatRecordingTime()
        val formattedDistance = this.distanceConverter.format(displayedDistance)

        return this.service.getString(R.string.activityrecorderservice_notification_contenttext_template, formattedDistance, recordingTimeDisplayTemplate)
    }

    private var isDestroyed = false
    public override fun destroy() {
        if(this.isDestroyed) {
            return
        }

        this.service.stopForeground(true)
        this.subscriptions.dispose()

        this.isDestroyed = true
    }

    companion object {
        public const val ACTION_RESUME = "trackrecorderservice.action.resume"

        public const val ACTION_PAUSE = "trackrecorderservice.action.pause"

        public const val ACTION_FINISH = "trackrecorderservice.action.finish"

        public const val ACTION_DISCARD = "trackrecorderservice.action.discard"

        public const val ID: Int = 1
    }
}