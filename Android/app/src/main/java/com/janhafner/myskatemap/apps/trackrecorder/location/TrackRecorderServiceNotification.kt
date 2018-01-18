package com.janhafner.myskatemap.apps.trackrecorder.location

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.TrackRecorderActivity
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.MySkatemapTrackRecorderAppChannel
import org.joda.time.Duration

internal final class TrackRecorderServiceNotification(private val context : Context) {
    private val notificationManager : NotificationManager = this.context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as NotificationManager

    private constructor(context : Context, trackRecorderServiceState : TrackRecorderServiceState, durationOfRecording : Duration? = null, trackLengthInMeters : Float? = null)
        : this(context) {
        this.update(trackRecorderServiceState, durationOfRecording, trackLengthInMeters)
    }

    public fun update(trackRecorderServiceState : TrackRecorderServiceState, durationOfRecording : Duration? = null, trackLengthInMeters : Float? = null) {
        val notificationCompatBuilder = NotificationCompat.Builder(context, MySkatemapTrackRecorderAppChannel.CHANNEL_ID)

        notificationCompatBuilder.setSmallIcon(R.mipmap.ic_launcher)
        notificationCompatBuilder.setContentTitle(context.getText(R.string.trackrecorderservice_notification_title))

        when(trackRecorderServiceState) {
            TrackRecorderServiceState.Ready ->
                notificationCompatBuilder.setContentText(context.getString(R.string.trackrecorderservice_notification_status_ready))
            TrackRecorderServiceState.Running ->
                notificationCompatBuilder.setContentText(context.getString(R.string.trackrecorderservice_notification_status_runnning))
            TrackRecorderServiceState.Paused ->
                notificationCompatBuilder.setContentText(context.getString(R.string.trackrecorderservice_notification_status_paused))
        }

        if(durationOfRecording != null) {
            notificationCompatBuilder.setSubText(durationOfRecording.toString())
        }

        if(trackLengthInMeters != null) {
            notificationCompatBuilder.setContentInfo(trackLengthInMeters.toString())
        }

        notificationCompatBuilder.setOngoing(true)
        notificationCompatBuilder.setContentIntent(PendingIntent.getActivity(context, 0, Intent(context, TrackRecorderActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT))

        val notification = notificationCompatBuilder.build()

        this.notificationManager.notify(Id, notification)
    }

    public fun close() {
        this.notificationManager.cancel(Id)
    }

    companion object {
        private val Id : Int = 1

        public fun showNew(context : Context, trackRecorderServiceState : TrackRecorderServiceState, durationOfRecording : Duration? = null, trackLengthInMeters : Float? = null) : TrackRecorderServiceNotification {
            return TrackRecorderServiceNotification(context, trackRecorderServiceState, durationOfRecording, trackLengthInMeters)
        }
    }
}