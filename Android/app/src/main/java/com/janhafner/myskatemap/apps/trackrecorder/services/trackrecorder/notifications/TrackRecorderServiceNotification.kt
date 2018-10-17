package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.notifications

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.common.IDestroyable
import com.janhafner.myskatemap.apps.trackrecorder.common.formatRecordingTime
import com.janhafner.myskatemap.apps.trackrecorder.conversion.distance.IDistanceConverter
import com.janhafner.myskatemap.apps.trackrecorder.conversion.distance.IDistanceConverterFactory
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance.format
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderService
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session.ITrackRecordingSession
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session.SessionStateInfo
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session.TrackRecordingSessionState
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session.TrackingPausedReason
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.TrackRecorderActivity
import io.reactivex.disposables.CompositeDisposable
import org.joda.time.Period


internal final class TrackRecorderServiceNotification(private val service: Service,
                                                         private val appSettings: IAppSettings,
                                                         trackRecordingSession: ITrackRecordingSession,
                                                         private val distanceConverterFactory: IDistanceConverterFactory) : IDestroyable {
    private val subscriptions: CompositeDisposable = CompositeDisposable()

    private var trackRecorderSession: ITrackRecordingSession? = null

    private var distanceConverter: IDistanceConverter

    private var state: SessionStateInfo = SessionStateInfo(TrackRecordingSessionState.Paused)

    private var recordingTime: Period? = null

    private var distance: Float? = null

    private var vibrateOnLocationAvailabilityLoss: Boolean = false

    private val notificationCompatBuilder: NotificationCompat.Builder

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.notificationCompatBuilder = NotificationCompat.Builder(this.service, TrackRecorderServiceNotificationChannel.ID)
        } else {
            @Suppress("DEPRECATION")
            this.notificationCompatBuilder = NotificationCompat.Builder(this.service)
        }

        this.distanceConverter = this.distanceConverterFactory.createConverter()

        this.notificationCompatBuilder.setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
        this.notificationCompatBuilder.setSmallIcon(R.drawable.ic_stat_track_recorder)
        this.notificationCompatBuilder.setShowWhen(false)
        this.notificationCompatBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        this.notificationCompatBuilder.setOngoing(true)

        val intent = Intent(this.service, TrackRecorderActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)

        val pendingIntent = PendingIntent.getActivity(this.service, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        this.notificationCompatBuilder.setContentIntent(pendingIntent)

        this.subscriptions.addAll(
                appSettings.propertyChanged
                        .filter {
                            it.hasChanged
                        }
                        .subscribe{
                            if(it.propertyName == IAppSettings::vibrateOnLocationAvailabilityLoss.name) {
                                this.vibrateOnLocationAvailabilityLoss = this.appSettings.vibrateOnLocationAvailabilityLoss

                                this.update()
                            }
                        }
        )

        this.trackRecorderSession = this.getInitializedSession(trackRecordingSession)
    }

    private fun getInitializedSession(trackRecorderSession: ITrackRecordingSession): ITrackRecordingSession {
        this.subscriptions.addAll(
                trackRecorderSession.recordingTimeChanged
                        .subscribe {
                            this.recordingTime = it

                            this.update()
                        },
                trackRecorderSession.stateChanged
                        .subscribe {
                            this.state = it

                            this.update()
                        },
                trackRecorderSession.distanceChanged
                        .subscribe {
                            this.distance = it

                            this.update()
                        }
        )

        return trackRecorderSession
    }

    private fun update() {
        this.notificationCompatBuilder.setVibrate(null)

        when(this.state.state) {
            TrackRecordingSessionState.Running ->
                this.notificationCompatBuilder.setContentTitle(this.service.getString(R.string.trackrecorderservice_notification_status_running))
            TrackRecordingSessionState.Paused ->
                if(this.state.pausedReason == TrackingPausedReason.LocationServicesUnavailable) {
                    this.notificationCompatBuilder.setContentTitle(this.service.getString(R.string.trackrecorderservice_notification_status_locationservicesunavailable))

                    if(this.vibrateOnLocationAvailabilityLoss) {
                        this.notificationCompatBuilder.setVibrate(longArrayOf(500, 500, 500, 500, 500, 500))
                    }

                } else {
                    this.notificationCompatBuilder.setContentTitle(this.service.getString(R.string.trackrecorderservice_notification_status_paused))
                }
        }

        val contentText = this.buildContentText()
        this.notificationCompatBuilder.setContentText(contentText)

        this.notificationCompatBuilder.mActions.clear()

        if (this.state.state == TrackRecordingSessionState.Paused) {
            if(this.state.pausedReason != TrackingPausedReason.LocationServicesUnavailable) {
                this.notificationCompatBuilder.addAction(NotificationCompat.Action.Builder(R.drawable.ic_play_arrow_bright_24dp, this.service.getString(R.string.trackrecorderservice_notification_action_resume), PendingIntent.getService(this.service, 0, Intent(ACTION_RESUME, null, this.service, TrackRecorderService::class.java), PendingIntent.FLAG_UPDATE_CURRENT)).build())
            }

        } else if (this.state.state == TrackRecordingSessionState.Running) {
            this.notificationCompatBuilder.addAction(NotificationCompat.Action.Builder(R.drawable.ic_pause_bright_24dp, this.service.getString(R.string.trackrecorderservice_notification_action_pause), PendingIntent.getService(this.service, 0, Intent(ACTION_PAUSE, null, this.service, TrackRecorderService::class.java), PendingIntent.FLAG_UPDATE_CURRENT)).build())
        }

        val notification = this.notificationCompatBuilder.build()
        this.service.startForeground(TrackRecorderServiceNotification.ID, notification)
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

        return this.service.getString(R.string.trackrecorderservice_notification_contenttext_template, formattedDistance, recordingTimeDisplayTemplate)
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

        public const val ID: Int = 1
    }
}