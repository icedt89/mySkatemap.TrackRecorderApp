package com.janhafner.myskatemap.apps.trackrecorder.location

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.activities.trackrecorder.TrackRecorderActivity
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.TrackRecorderServiceNotificationChannel
import com.janhafner.myskatemap.apps.trackrecorder.roundToDecimalPlaces
import org.joda.time.Period
import org.joda.time.format.PeriodFormatter
import org.joda.time.format.PeriodFormatterBuilder

internal final class TrackRecorderServiceNotification(private val context: Context) {
    private val notificationManager: NotificationManager = this.context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as NotificationManager

    private constructor(context: Context, trackRecorderServiceState: TrackRecorderServiceState, durationOfRecording: Period? = null, trackLengthInMeters: Float? = null)
       : this(context) {
        this.update(trackRecorderServiceState, durationOfRecording, trackLengthInMeters)
    }

    public fun update(trackRecorderServiceState: TrackRecorderServiceState, durationOfRecording: Period? = null, trackLengthInMeters: Float? = null) {
        val notificationCompatBuilder = NotificationCompat.Builder(this.context, TrackRecorderServiceNotificationChannel.Id)

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
                val durationDisplayTemplate = context.getString(R.string.trackrecorderservice_notification_recordingduration_template, TrackRecorderServiceNotification.durationFormatter.print(durationOfRecording))
                notificationCompatBuilder.setSubText(durationDisplayTemplate)
            }

            if (trackLengthInMeters != null) {
                 val lengthDisplayTemplate = context.getString(R.string.trackrecorderservice_notification_tracklength_template, trackLengthInMeters!!.roundToDecimalPlaces())
                notificationCompatBuilder.setContentInfo(lengthDisplayTemplate)
            }

            if(trackRecorderServiceState == TrackRecorderServiceState.Paused) {
                notificationCompatBuilder.addAction(R.mipmap.ic_play_arrow_white_48dp, this.context.getString(R.string.trackrecorderservice_notification_action_resume), PendingIntent.getService(this.context, 0, Intent("trackrecorderservice.action.resume", null, this.context, TrackRecorderService::class.java), PendingIntent.FLAG_UPDATE_CURRENT))
            } else if(trackRecorderServiceState == TrackRecorderServiceState.Running) {
                notificationCompatBuilder.addAction(R.mipmap.ic_pause_white_48dp, this.context.getString(R.string.trackrecorderservice_notification_action_pause), PendingIntent.getService(this.context, 0, Intent("trackrecorderservice.action.pause", null, this.context, TrackRecorderService::class.java), PendingIntent.FLAG_UPDATE_CURRENT))
            }
        }

        notificationCompatBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        notificationCompatBuilder.setOngoing(true)
        notificationCompatBuilder.setContentIntent(PendingIntent.getActivity(this.context, 0, Intent(this.context, TrackRecorderActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT))

        val notification = notificationCompatBuilder.build()

        this.notificationManager.notify(Id, notification)
    }

    public fun close() {
        this.notificationManager.cancel(Id)
    }

    companion object {
        private val Id: Int = 1

        private val durationFormatter: PeriodFormatter = PeriodFormatterBuilder()
                .minimumPrintedDigits(2)
                .printZeroAlways()
                .appendHours()
                .appendSeparator(":")
                .appendMinutes()
                .appendSeparator(":")
                .appendSeconds()
                .toFormatter()

        public fun showNew(context: Context, trackRecorderServiceState: TrackRecorderServiceState, durationOfRecording: Period? = null, trackLengthInMeters: Float? = null): TrackRecorderServiceNotification {
            return TrackRecorderServiceNotification(context, trackRecorderServiceState, durationOfRecording, trackLengthInMeters)
        }
    }
}