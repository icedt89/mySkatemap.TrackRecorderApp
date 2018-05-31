package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.refactored

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.formatRecordingTime
import com.janhafner.myskatemap.apps.trackrecorder.services.distance.ITrackDistanceUnitFormatter
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderService
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceState
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.notifications.TrackRecorderServiceNotificationChannel
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.ActivityStartMode
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.TrackRecorderActivity
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.TrackRecorderActivityPresenter
import org.joda.time.Period

internal final class RefactoredTrackRecorderServiceNotification(private val context: Context,
                                                                public var trackDistanceUnitFormatter: ITrackDistanceUnitFormatter) {
    public var state: TrackRecorderServiceState = TrackRecorderServiceState.Idle

    public var durationOfRecording: Period? = null

    public var trackDistance: Float? = null

    public var isBound: Boolean = false

    private val notificationCompatBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this.context, TrackRecorderServiceNotificationChannel.ID)

    init {
        this.notificationCompatBuilder.setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
        this.notificationCompatBuilder.setSmallIcon(R.drawable.ic_stat_track_recorder)
        this.notificationCompatBuilder.setShowWhen(false)
        this.notificationCompatBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        this.notificationCompatBuilder.setOngoing(true)

        val intent = Intent(this.context, TrackRecorderActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                .putExtra(TrackRecorderActivityPresenter.ACTIVITY_START_MODE_KEY, ActivityStartMode.TryResume.toString())

        val pendingIntent = PendingIntent.getActivity(this.context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        this.notificationCompatBuilder.setContentIntent(pendingIntent)
    }

    public fun update() : Notification? {
        when(this.state) {
            TrackRecorderServiceState.Idle ->
                return null
            TrackRecorderServiceState.Running ->
                this.notificationCompatBuilder.setContentTitle(this.context.getString(R.string.trackrecorderservice_notification_status_running))
            TrackRecorderServiceState.LocationServicesUnavailable -> {
                this.notificationCompatBuilder.setContentTitle(this.context.getString(R.string.trackrecorderservice_notification_status_locationservicesunavailable))
            }
            TrackRecorderServiceState.Paused ->
                this.notificationCompatBuilder.setContentTitle(this.context.getString(R.string.trackrecorderservice_notification_status_paused))
        }

        this.notificationCompatBuilder.mActions.clear()

        if (this.state != TrackRecorderServiceState.Idle) {
            val contentText = this.buildContentText()
            this.notificationCompatBuilder.setContentText(contentText)

            if (this.state == TrackRecorderServiceState.Paused) {
                this.notificationCompatBuilder.addAction(NotificationCompat.Action.Builder(R.drawable.ic_action_track_recorder_recording_startresume, this.context.getString(R.string.trackrecorderservice_notification_action_resume), PendingIntent.getService(this.context, 0, Intent(ACTION_RESUME, null, this.context, TrackRecorderService::class.java), PendingIntent.FLAG_UPDATE_CURRENT)).build())
            } else if (this.state == TrackRecorderServiceState.Running) {
                this.notificationCompatBuilder.addAction(NotificationCompat.Action.Builder(R.drawable.ic_action_track_recorder_recording_pause, this.context.getString(R.string.trackrecorderservice_notification_action_pause), PendingIntent.getService(this.context, 0, Intent(ACTION_PAUSE, null, this.context, TrackRecorderService::class.java), PendingIntent.FLAG_UPDATE_CURRENT)).build())
            }
        }

        if(!this.isBound && this.state != TrackRecorderServiceState.Running){
            this.notificationCompatBuilder.addAction(NotificationCompat.Action.Builder(R.drawable.ic_action_track_recorder_service_terminate, this.context.getString(R.string.trackrecorderservice_notification_action_terminate), PendingIntent.getService(this.context, 0, Intent(ACTION_TERMINATE, null, this.context, TrackRecorderService::class.java), PendingIntent.FLAG_UPDATE_CURRENT)).build())
        }

        return this.notificationCompatBuilder.build()
    }

    private fun buildContentText(): String {
        if(this.durationOfRecording == null || this.trackDistance == null || this.durationOfRecording?.seconds == 0 || this.trackDistance == 0f) {
            return ""
        }

        val durationDisplayTemplate = this.durationOfRecording!!.formatRecordingTime()
        val formattedTrackDistance = this.trackDistanceUnitFormatter.format(this.trackDistance!!)

        return this.context.getString(R.string.trackrecorderservice_notification_contenttext_template, formattedTrackDistance, durationDisplayTemplate)
    }

    companion object {
        public const val ACTION_RESUME = "trackrecorderservice.action.resume"

        public const val ACTION_PAUSE = "trackrecorderservice.action.pause"

        public const val ACTION_TERMINATE = "trackrecorderservice.action.terminate"

        public const val ID: Int = 1
    }
}