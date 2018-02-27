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

    private val notificationCompatBuilder:NotificationCompat.Builder = NotificationCompat.Builder(this.context, TrackRecorderServiceNotificationChannel.ID)

    init {
        this.notificationCompatBuilder.setSmallIcon(R.drawable.ic_stat_track_recorder)
        this.notificationCompatBuilder.setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
        this.notificationCompatBuilder.setContentTitle(context.getText(R.string.trackrecorderservice_notification_title))

        this.notificationCompatBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        this.notificationCompatBuilder.setOngoing(true)
        this.notificationCompatBuilder.setContentIntent(PendingIntent.getActivity(this.context, 0, Intent(this.context, TrackRecorderActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT))
    }

    public fun update() {
        when(this.state) {
            TrackRecorderServiceState.Initializing ->
                this.notificationCompatBuilder.setContentText(context.getString(R.string.trackrecorderservice_notification_status_initializing))
            TrackRecorderServiceState.Ready ->
                this.notificationCompatBuilder.setContentText(context.getString(R.string.trackrecorderservice_notification_status_ready))
            TrackRecorderServiceState.Running ->
                this.notificationCompatBuilder.setContentText(context.getString(R.string.trackrecorderservice_notification_status_runnning))
            TrackRecorderServiceState.LocationServicesUnavailable ->
                this.notificationCompatBuilder.setContentText(context.getString(R.string.trackrecorderservice_notification_status_locationservicesunavailable))
            TrackRecorderServiceState.Paused ->
                this.notificationCompatBuilder.setContentText(context.getString(R.string.trackrecorderservice_notification_status_paused))
        }

        this.notificationCompatBuilder.mActions.clear()

        if (this.state != TrackRecorderServiceState.Initializing) {
            if (this.durationOfRecording != null) {
                val durationDisplayTemplate = context.getString(R.string.trackrecorderservice_notification_recordingduration_template, this.durationOfRecording!!.formatRecordingTime())
                this.notificationCompatBuilder.setSubText(durationDisplayTemplate)
            }

            if (this.trackDistance != null) {
                val lengthDisplayTemplate = this.trackDistance!!.formatTrackDistance(this.context)
                this.notificationCompatBuilder.setContentInfo(lengthDisplayTemplate)
            }

            if(this.state == TrackRecorderServiceState.Paused) {
                this.notificationCompatBuilder.addAction(R.drawable.ic_action_track_recorder_record_startresume, this.context.getString(R.string.trackrecorderservice_notification_action_resume), PendingIntent.getService(this.context, 0, Intent(ACTION_RESUME, null, this.context, TrackRecorderService::class.java), PendingIntent.FLAG_UPDATE_CURRENT))
            } else if(this.state == TrackRecorderServiceState.Running) {
                this.notificationCompatBuilder.addAction(R.drawable.ic_action_track_recorder_recording_pause, this.context.getString(R.string.trackrecorderservice_notification_action_pause), PendingIntent.getService(this.context, 0, Intent(ACTION_PAUSE, null, this.context, TrackRecorderService::class.java), PendingIntent.FLAG_UPDATE_CURRENT))
            }
        }

        this.notificationCompatBuilder.addAction(R.drawable.ic_action_track_recorder_service_terminate, this.context.getString(R.string.trackrecorderservice_notification_action_terminate), PendingIntent.getService(this.context, 0, Intent(ACTION_TERMINATE, null, this.context, TrackRecorderService::class.java), PendingIntent.FLAG_UPDATE_CURRENT))

        val notification = this.notificationCompatBuilder.build()

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