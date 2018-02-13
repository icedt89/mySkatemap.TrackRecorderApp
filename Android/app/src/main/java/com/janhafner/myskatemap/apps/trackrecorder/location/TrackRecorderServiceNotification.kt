package com.janhafner.myskatemap.apps.trackrecorder.location

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.activities.trackrecorder.TrackRecorderActivity
import com.janhafner.myskatemap.apps.trackrecorder.formatRecordingTime
import com.janhafner.myskatemap.apps.trackrecorder.formatTrackDistance
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.TrackRecorderServiceNotificationChannel
import org.joda.time.Period

internal final class TrackRecorderServiceNotification(private val context: Context) {
    private val notificationManager: NotificationManager = this.context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as NotificationManager

    private constructor(context: Context, trackRecorderServiceState: TrackRecorderServiceState, durationOfRecording: Period? = null, trackLengthInMeters: Float? = null)
       : this(context) {
        this.update(trackRecorderServiceState, durationOfRecording, trackLengthInMeters)
    }

    public fun update(trackRecorderServiceState: TrackRecorderServiceState, durationOfRecording: Period? = null, trackLengthInMeters: Float? = null) {
        val notificationCompatBuilder = NotificationCompat.Builder(this.context, TrackRecorderServiceNotificationChannel.ID)

        notificationCompatBuilder.setSmallIcon(R.drawable.ic_launcher, NotificationCompat.BADGE_ICON_LARGE)
        notificationCompatBuilder.setBadgeIconType(R.drawable.ic_launcher)
        notificationCompatBuilder.setContentTitle(context.getText(R.string.trackrecorderservice_notification_title))

        when(trackRecorderServiceState) {
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

        if (trackRecorderServiceState != TrackRecorderServiceState.Initializing) {
            if (durationOfRecording != null) {
                val durationDisplayTemplate = context.getString(R.string.trackrecorderservice_notification_recordingduration_template, durationOfRecording.formatRecordingTime())
                notificationCompatBuilder.setSubText(durationDisplayTemplate)
            }

            if (trackLengthInMeters != null) {
                 val lengthDisplayTemplate = trackLengthInMeters.formatTrackDistance(this.context)
                notificationCompatBuilder.setContentInfo(lengthDisplayTemplate)
            }

            if(trackRecorderServiceState == TrackRecorderServiceState.Paused) {
                notificationCompatBuilder.addAction(R.mipmap.ic_play_arrow_white_48dp, this.context.getString(R.string.trackrecorderservice_notification_action_resume), PendingIntent.getService(this.context, 0, Intent(TrackRecorderServiceNotification.ACTION_RESUME, null, this.context, TrackRecorderService::class.java), PendingIntent.FLAG_UPDATE_CURRENT))
            } else if(trackRecorderServiceState == TrackRecorderServiceState.Running) {
                notificationCompatBuilder.addAction(R.mipmap.ic_pause_white_48dp, this.context.getString(R.string.trackrecorderservice_notification_action_pause), PendingIntent.getService(this.context, 0, Intent(TrackRecorderServiceNotification.ACTION_PAUSE, null, this.context, TrackRecorderService::class.java), PendingIntent.FLAG_UPDATE_CURRENT))
            }
        }

        notificationCompatBuilder.addAction(R.mipmap.ic_close_white_48dp, this.context.getString(R.string.trackrecorderservice_notification_action_terminate), PendingIntent.getService(this.context, 0, Intent(TrackRecorderServiceNotification.ACTION_TERMINATE, null, this.context, TrackRecorderService::class.java), PendingIntent.FLAG_UPDATE_CURRENT))

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

        public fun showNew(context: Context, trackRecorderServiceState: TrackRecorderServiceState, durationOfRecording: Period? = null, trackLengthInMeters: Float? = null): TrackRecorderServiceNotification {
            return TrackRecorderServiceNotification(context, trackRecorderServiceState, durationOfRecording, trackLengthInMeters)
        }
    }
}