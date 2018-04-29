package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.notifications

import android.app.PendingIntent
import android.content.Intent
import android.support.v4.app.NotificationCompat
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.formatRecordingTime
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance.ITrackDistanceUnitFormatter
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.settings.AppSettings
import com.janhafner.myskatemap.apps.trackrecorder.location.TrackRecorderServiceState
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderService
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.ActivityStartMode
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.TrackRecorderActivity
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.TrackRecorderActivityPresenter
import org.joda.time.Period

internal final class TrackRecorderServiceNotification(private val trackRecorderService: TrackRecorderService, public var trackDistanceUnitFormatter: ITrackDistanceUnitFormatter) {
    public var state: TrackRecorderServiceState = TrackRecorderServiceState.Idle

    public var durationOfRecording: Period? = null

    public var trackDistance: Float? = null

    public var vibrateOnLocationUnavailableState: Boolean = AppSettings.DEFAULT_VIBRATE_ON_BACKGROUND_STOP

    public var flashColorOnLocationUnavailableState: Int? = AppSettings.DEFAULT_NOTIFICATION_FLASH_COLOR_ON_BACKGROUND_STOP

    public var isBound: Boolean = false

    private val notificationCompatBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this.trackRecorderService, TrackRecorderServiceNotificationChannel.ID)

    init {
        this.notificationCompatBuilder.setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
        this.notificationCompatBuilder.setSmallIcon(R.drawable.ic_stat_track_recorder)
        this.notificationCompatBuilder.setShowWhen(false)
        this.notificationCompatBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        this.notificationCompatBuilder.setOngoing(true)

        val intent = Intent(this.trackRecorderService, TrackRecorderActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        intent.putExtra(TrackRecorderActivityPresenter.ACTIVITY_START_MODE_KEY, ActivityStartMode.TryResume.toString())

        val pendingIntent = PendingIntent.getActivity(this.trackRecorderService, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        this.notificationCompatBuilder.setContentIntent(pendingIntent)
    }

    public fun update() {
        when(this.state) {
            TrackRecorderServiceState.Running ->
                this.notificationCompatBuilder.setContentTitle(trackRecorderService.getString(R.string.trackrecorderservice_notification_status_running))
            TrackRecorderServiceState.LocationServicesUnavailable -> {
                this.notificationCompatBuilder.setContentTitle(trackRecorderService.getString(R.string.trackrecorderservice_notification_status_locationservicesunavailable))
                if(this.vibrateOnLocationUnavailableState) {
                    this.notificationCompatBuilder.setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000))
                } else {
                    this.notificationCompatBuilder.setVibrate(LongArray(0))
                }

                if(this.flashColorOnLocationUnavailableState != null) {
                    this.notificationCompatBuilder.setLights(this.flashColorOnLocationUnavailableState!!, 1000, 1000)
                } else {
                    this.notificationCompatBuilder.setLights(0,0,0)
                }
            }
            TrackRecorderServiceState.Paused ->
                this.notificationCompatBuilder.setContentTitle(trackRecorderService.getString(R.string.trackrecorderservice_notification_status_paused))
            else -> {
                // Nothing happens here. Else branch exist only to prevent warning on compile Oo
            }
        }

        this.notificationCompatBuilder.mActions.clear()

        if (this.state != TrackRecorderServiceState.Idle) {
            val contentText = this.buildContentText()
            this.notificationCompatBuilder.setContentText(contentText)

            if (this.state == TrackRecorderServiceState.Paused) {
                this.notificationCompatBuilder.addAction(NotificationCompat.Action.Builder(R.drawable.ic_action_track_recorder_recording_startresume, this.trackRecorderService.getString(R.string.trackrecorderservice_notification_action_resume), PendingIntent.getService(this.trackRecorderService, 0, Intent(ACTION_RESUME, null, this.trackRecorderService, TrackRecorderService::class.java), PendingIntent.FLAG_UPDATE_CURRENT)).build())
            } else if (this.state == TrackRecorderServiceState.Running) {
                this.notificationCompatBuilder.addAction(NotificationCompat.Action.Builder(R.drawable.ic_action_track_recorder_recording_pause, this.trackRecorderService.getString(R.string.trackrecorderservice_notification_action_pause), PendingIntent.getService(this.trackRecorderService, 0, Intent(ACTION_PAUSE, null, this.trackRecorderService, TrackRecorderService::class.java), PendingIntent.FLAG_UPDATE_CURRENT)).build())
            }
        }

        if(this.isBound && this.state != TrackRecorderServiceState.Running){
            this.notificationCompatBuilder.addAction(NotificationCompat.Action.Builder(R.drawable.ic_action_track_recorder_service_terminate, this.trackRecorderService.getString(R.string.trackrecorderservice_notification_action_terminate), PendingIntent.getService(this.trackRecorderService, 0, Intent(ACTION_TERMINATE, null, this.trackRecorderService, TrackRecorderService::class.java), PendingIntent.FLAG_UPDATE_CURRENT)).build())
        }

        val notification = this.notificationCompatBuilder.build()

        this.trackRecorderService.startForeground(TrackRecorderServiceNotification.ID, notification)
    }

    private fun buildContentText(): String {
        if(this.durationOfRecording == null || this.trackDistance == null || this.durationOfRecording?.seconds == 0 || this.trackDistance == 0f) {
            return ""
        }

        val durationDisplayTemplate = this.durationOfRecording!!.formatRecordingTime()
        val formattedTrackDistance = this.trackDistanceUnitFormatter.format(this.trackDistance!!)

        return trackRecorderService.getString(R.string.trackrecorderservice_notification_contenttext_template, formattedTrackDistance, durationDisplayTemplate)
    }

    public fun close() {
        this.trackRecorderService.stopForeground(true)
    }

    companion object {
        public const val ACTION_RESUME = "trackrecorderservice.action.resume"

        public const val ACTION_PAUSE = "trackrecorderservice.action.pause"

        public const val ACTION_FINISH = "traclrecorderservoce.action.finish"

        public const val ACTION_DISCARD = "traclrecorderservoce.action.discard"

        public const val ACTION_TERMINATE = "trackrecorderservice.action.terminate"

        private const val ID: Int = 1
    }
}