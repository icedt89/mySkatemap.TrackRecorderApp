package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.notifications

import android.app.PendingIntent
import android.content.Intent
import android.support.v4.app.NotificationCompat
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.formatRecordingTime
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance.ITrackDistanceUnitFormatter
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance.KilometersTrackDistanceUnitFormatter
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.settings.AppSettings
import com.janhafner.myskatemap.apps.trackrecorder.location.TrackRecorderServiceState
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderService
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.TrackRecorderActivity
import org.joda.time.Period

internal final class TrackRecorderServiceNotification(private val trackRecorderService: TrackRecorderService) {
    public var state: TrackRecorderServiceState = TrackRecorderServiceState.Initializing

    public var durationOfRecording: Period? = null

    public var trackDistance: Float? = null

    public var VibrateOnLocationUnavailableState: Boolean = AppSettings.DEFAULT_VIBRATE_ON_BACKGROUND_STOP

    public var flashColorOnLocationUnavailableState: Int? = AppSettings.DEFAULT_NOTIFICATION_FLASH_COLOR_ON_BACKGROUND_STOP

    public var userInitiatedServiceTerminationAllowed: Boolean = false

    @Deprecated("Resolve using ITrackDistanceUnitFormatterFactory by using Dagger")
    private val trackDistanceUnitFormatter: ITrackDistanceUnitFormatter = KilometersTrackDistanceUnitFormatter()

    private val notificationCompatBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this.trackRecorderService, TrackRecorderServiceNotificationChannel.ID)

    init {
        this.notificationCompatBuilder.setSmallIcon(R.drawable.ic_stat_track_recorder)
        this.notificationCompatBuilder.setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
        this.notificationCompatBuilder.setContentTitle(trackRecorderService.getText(R.string.trackrecorderservice_notification_title))

        this.notificationCompatBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        this.notificationCompatBuilder.setOngoing(true)
        this.notificationCompatBuilder.setContentIntent(PendingIntent.getActivity(this.trackRecorderService, 0, Intent(this.trackRecorderService, TrackRecorderActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT))
    }

    public fun update() {
        when(this.state) {
            TrackRecorderServiceState.Ready ->
                this.notificationCompatBuilder.setContentText(trackRecorderService.getString(R.string.trackrecorderservice_notification_status_ready))
            TrackRecorderServiceState.Running ->
                this.notificationCompatBuilder.setContentText(trackRecorderService.getString(R.string.trackrecorderservice_notification_status_runnning))
            TrackRecorderServiceState.LocationServicesUnavailable -> {
                this.notificationCompatBuilder.setContentText(trackRecorderService.getString(R.string.trackrecorderservice_notification_status_locationservicesunavailable))
                if(this.VibrateOnLocationUnavailableState) {
                    this.notificationCompatBuilder.setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
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
                this.notificationCompatBuilder.setContentText(trackRecorderService.getString(R.string.trackrecorderservice_notification_status_paused))
        }

        this.notificationCompatBuilder.mActions.clear()

        if (this.state != TrackRecorderServiceState.Initializing) {
            if (this.durationOfRecording != null) {
                val durationDisplayTemplate = trackRecorderService.getString(R.string.trackrecorderservice_notification_recordingduration_template, this.durationOfRecording!!.formatRecordingTime())
                this.notificationCompatBuilder.setSubText(durationDisplayTemplate)
            }

            if (this.trackDistance != null) {
                val formattedTrackDistance = this.trackDistanceUnitFormatter.format(this.trackRecorderService, this.trackDistance!!)

                this.notificationCompatBuilder.setContentInfo(formattedTrackDistance)
            }

            if (this.state == TrackRecorderServiceState.Paused) {
                this.notificationCompatBuilder.addAction(R.drawable.ic_action_track_recorder_recording_startresume, this.trackRecorderService.getString(R.string.trackrecorderservice_notification_action_resume), PendingIntent.getService(this.trackRecorderService, 0, Intent(ACTION_RESUME, null, this.trackRecorderService, TrackRecorderService::class.java), PendingIntent.FLAG_UPDATE_CURRENT))
            } else if(this.state == TrackRecorderServiceState.Running) {
                this.notificationCompatBuilder.addAction(R.drawable.ic_action_track_recorder_recording_pause, this.trackRecorderService.getString(R.string.trackrecorderservice_notification_action_pause), PendingIntent.getService(this.trackRecorderService, 0, Intent(ACTION_PAUSE, null, this.trackRecorderService, TrackRecorderService::class.java), PendingIntent.FLAG_UPDATE_CURRENT))
            } else if(this.state == TrackRecorderServiceState.LocationServicesUnavailable) {
                this.notificationCompatBuilder.addAction(R.drawable.ic_action_track_recorder_service_showlocationservices, this.trackRecorderService.getString(R.string.trackrecorderservice_notification_action_showlocationservices), PendingIntent.getService(this.trackRecorderService, 0, Intent(ACTION_SHOW_LOCATION_SERVICES, null, this.trackRecorderService, TrackRecorderService::class.java), PendingIntent.FLAG_UPDATE_CURRENT))
            }
        }

        if(this.userInitiatedServiceTerminationAllowed){
            this.notificationCompatBuilder.addAction(R.drawable.ic_action_track_recorder_service_terminate, this.trackRecorderService.getString(R.string.trackrecorderservice_notification_action_terminate), PendingIntent.getService(this.trackRecorderService, 0, Intent(ACTION_TERMINATE, null, this.trackRecorderService, TrackRecorderService::class.java), PendingIntent.FLAG_UPDATE_CURRENT))
        }

        val notification = this.notificationCompatBuilder.build()

        this.trackRecorderService.startForeground(TrackRecorderServiceNotification.ID, notification)
    }

    public fun close() {
        this.trackRecorderService.stopForeground(true)
    }

    companion object {
        public const val ACTION_RESUME = "trackrecorderservice.action.resume"

        public const val ACTION_PAUSE = "trackrecorderservice.action.pause"

        public const val ACTION_TERMINATE = "trackrecorderservice.action.terminate"

        public const val ACTION_SHOW_LOCATION_SERVICES = "trackrecorderservice.action.showlocationservices"

        private const val ID: Int = 1
    }
}