package com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.annotation.RequiresApi


@RequiresApi(Build.VERSION_CODES.O)
internal final class ActivityRecorderServiceNotificationChannel(notificationManager: NotificationManager) {
    init {
        val notificationChannel = NotificationChannel(ActivityRecorderServiceNotificationChannel.ID, "Tracking Channel", NotificationManager.IMPORTANCE_LOW)
        notificationChannel.description = "Tracking Channel Description"

        notificationManager.createNotificationChannel(notificationChannel)
    }

    companion object {
        public const val ID = "MySkatemapTrackRecorderApp_Tracking"
    }
}