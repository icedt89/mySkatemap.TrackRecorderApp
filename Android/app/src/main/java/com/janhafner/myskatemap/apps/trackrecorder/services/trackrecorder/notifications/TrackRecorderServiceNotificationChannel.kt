package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.annotation.RequiresApi


@RequiresApi(Build.VERSION_CODES.O)
internal final class TrackRecorderServiceNotificationChannel(notificationManager: NotificationManager) {
    init {
        val notificationChannel = NotificationChannel(TrackRecorderServiceNotificationChannel.ID, "Tracking Channel", NotificationManager.IMPORTANCE_LOW)
        notificationChannel.description = "Tracking Channel Description"

        notificationManager.createNotificationChannel(notificationChannel)
    }

    companion object {
        public const val ID = "MySkatemapTrackRecorderApp_Tracking"
    }
}