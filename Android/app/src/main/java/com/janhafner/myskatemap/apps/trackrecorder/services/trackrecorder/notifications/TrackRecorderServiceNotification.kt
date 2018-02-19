package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.formatRecordingTime
import com.janhafner.myskatemap.apps.trackrecorder.formatTrackDistance
import com.janhafner.myskatemap.apps.trackrecorder.location.TrackRecorderServiceState
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderService
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.TrackRecorderActivity
import org.joda.time.Period

internal final class TrackRecorderServiceNotification(private val context: Context) {
    private val notificationManager: NotificationManager = this.context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as NotificationManager

    public var state: TrackRecorderServiceState = TrackRecorderServiceState.Initializing

    public var durationOfRecording: Period? = null

    public var trackDistance: Float? = null

    public fun update() {
        val notificationCompatBuilder = NotificationCompat.Builder(this.context, TrackRecorderServiceNotificationChannel.ID)

        notificationCompatBuilder.setSmallIcon(R.drawable.ic_stat_track_recorder)
        notificationCompatBuilder.setBadgeIconType(NotificationCompat.BADGE_ICON_NONE) //*_LARGE
        notificationCompatBuilder.setContentTitle(context.getText(R.string.trackrecorderservice_notification_title))

        when(this.state) {
            TrackRecorderServiceState.Initializing ->
                notificationCompatBuilder.setContentText(context.getString(R.string.trackrecorderservice_notification_status_initializing))
            TrackRecorderServiceState.Ready ->
                notificationCompatBuilder.setContentText(context.getString(R.string.trackrecorderservice_notification_status_ready))
            TrackRecorderServiceState.Running ->
                notificationCompatBuilder.setContentText(context.getString(R.string.trackrecorderservice_notification_status_runnning))
            TrackRecorderServiceState.LocationServicesUnavailable ->
                notificationCompatBuilder.setContentText(context.getString(R.string.trackrecorderservice_notification_status_locationservicesunavailable))
            TrackRecorderServiceState.Paused ->
                notificationCompatBuilder.setContentText(context.getString(R.string.trackrecorderservice_notification_status_paused))
        }

        if (this.state != TrackRecorderServiceState.Initializing) {
            if (this.durationOfRecording != null) {
                val durationDisplayTemplate = context.getString(R.string.trackrecorderservice_notification_recordingduration_template, this.durationOfRecording!!.formatRecordingTime())
                notificationCompatBuilder.setSubText(durationDisplayTemplate)
            }

            if (this.trackDistance != null) {
                val lengthDisplayTemplate = this.trackDistance!!.formatTrackDistance(this.context)
                notificationCompatBuilder.setContentInfo(lengthDisplayTemplate)
            }

            if(this.state == TrackRecorderServiceState.Paused) {
                notificationCompatBuilder.addAction(R.drawable.ic_action_track_recorder_record_startresume, this.context.getString(R.string.trackrecorderservice_notification_action_resume), PendingIntent.getService(this.context, 0, Intent(ACTION_RESUME, null, this.context, TrackRecorderService::class.java), PendingIntent.FLAG_UPDATE_CURRENT))
            } else if(this.state == TrackRecorderServiceState.Running) {
                notificationCompatBuilder.addAction(R.drawable.ic_action_track_recorder_recording_pause, this.context.getString(R.string.trackrecorderservice_notification_action_pause), PendingIntent.getService(this.context, 0, Intent(ACTION_PAUSE, null, this.context, TrackRecorderService::class.java), PendingIntent.FLAG_UPDATE_CURRENT))
            }
        }

        notificationCompatBuilder.addAction(R.drawable.ic_action_track_recorder_service_terminate, this.context.getString(R.string.trackrecorderservice_notification_action_terminate), PendingIntent.getService(this.context, 0, Intent(ACTION_TERMINATE, null, this.context, TrackRecorderService::class.java), PendingIntent.FLAG_UPDATE_CURRENT))

        notificationCompatBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        notificationCompatBuilder.setOngoing(true)
        notificationCompatBuilder.setContentIntent(PendingIntent.getActivity(this.context, 0, Intent(this.context, TrackRecorderActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT))

        val notification = notificationCompatBuilder.build()

        this.notificationManager.notify(ID, notification)
    }

    public fun close() {
        this.notificationManager.cancel(ID)
    }

    companion object {
        public const val ACTION_RESUME = "trackrecorderservice.action.resume"

        public const val ACTION_PAUSE = "trackrecorderservice.action.pause"

        public const val ACTION_TERMINATE = "trackrecorderservice.action.terminate"

        private const val ID: Int = 1
    }
}