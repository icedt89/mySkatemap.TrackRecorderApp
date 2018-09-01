package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.notifications

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.formatRecordingTime
import com.janhafner.myskatemap.apps.trackrecorder.formatting.distance.IDistanceUnitFormatter
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderService
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecordingSessionState
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.TrackRecorderActivity
import org.joda.time.Period

internal final class TrackRecorderServiceNotification(private val context: Context,
                                                      public var distanceUnitFormatter: IDistanceUnitFormatter) {
    public var state: TrackRecordingSessionState = TrackRecordingSessionState.Paused

    public var recordingTime: Period? = null

    public var distance: Float? = null

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

        val pendingIntent = PendingIntent.getActivity(this.context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        this.notificationCompatBuilder.setContentIntent(pendingIntent)
    }

    public fun update() : Notification? {
        when(this.state) {
            TrackRecordingSessionState.Running ->
                this.notificationCompatBuilder.setContentTitle(this.context.getString(R.string.trackrecorderservice_notification_status_running))
            TrackRecordingSessionState.LocationServicesUnavailable -> {
                this.notificationCompatBuilder.setContentTitle(this.context.getString(R.string.trackrecorderservice_notification_status_locationservicesunavailable))
            }
            TrackRecordingSessionState.Paused ->
                this.notificationCompatBuilder.setContentTitle(this.context.getString(R.string.trackrecorderservice_notification_status_paused))
        }

        this.notificationCompatBuilder.mActions.clear()

        val contentText = this.buildContentText()
        this.notificationCompatBuilder.setContentText(contentText)

        if (this.state == TrackRecordingSessionState.Paused) {
            this.notificationCompatBuilder.addAction(NotificationCompat.Action.Builder(R.drawable.ic_play_arrow_bright_24dp, this.context.getString(R.string.trackrecorderservice_notification_action_resume), PendingIntent.getService(this.context, 0, Intent(ACTION_RESUME, null, this.context, TrackRecorderService::class.java), PendingIntent.FLAG_UPDATE_CURRENT)).build())
        } else if (this.state == TrackRecordingSessionState.Running) {
            this.notificationCompatBuilder.addAction(NotificationCompat.Action.Builder(R.drawable.ic_pause_bright_24dp, this.context.getString(R.string.trackrecorderservice_notification_action_pause), PendingIntent.getService(this.context, 0, Intent(ACTION_PAUSE, null, this.context, TrackRecorderService::class.java), PendingIntent.FLAG_UPDATE_CURRENT)).build())
        }

        if(!this.isBound && this.state != TrackRecordingSessionState.Running){
            this.notificationCompatBuilder.addAction(NotificationCompat.Action.Builder(R.drawable.ic_clear_bright_24dp, this.context.getString(R.string.trackrecorderservice_notification_action_terminate), PendingIntent.getService(this.context, 0, Intent(ACTION_TERMINATE, null, this.context, TrackRecorderService::class.java), PendingIntent.FLAG_UPDATE_CURRENT)).build())
        }

        return this.notificationCompatBuilder.build()
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

        val recordingTimeDisplayTemplate = displayedRecordingTime.formatRecordingTime()
        val formattedDistance = this.distanceUnitFormatter.format(displayedDistance)

        return this.context.getString(R.string.trackrecorderservice_notification_contenttext_template, formattedDistance, recordingTimeDisplayTemplate)
    }

    companion object {
        public const val ACTION_RESUME = "trackrecorderservice.action.resume"

        public const val ACTION_PAUSE = "trackrecorderservice.action.pause"

        public const val ACTION_TERMINATE = "trackrecorderservice.action.terminate"

        public const val ID: Int = 1
    }
}