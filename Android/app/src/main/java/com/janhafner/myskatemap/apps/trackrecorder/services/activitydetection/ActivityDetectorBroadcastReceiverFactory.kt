package com.janhafner.myskatemap.apps.trackrecorder.services.activitydetection

import android.content.Context
import com.google.android.gms.location.ActivityRecognitionClient
import com.janhafner.myskatemap.apps.trackrecorder.BuildConfig
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings

internal final class ActivityDetectorBroadcastReceiverFactory(private val context: Context, private val activityRecognitionClient: ActivityRecognitionClient, private val appSettings: IAppSettings) : IActivityDetectorBroadcastReceiverFactory {
    private val stillDetectorBroadcastReceiver: ActivityDetectorBroadcastReceiverBase = ActivityDetectorBroadcastReceiver(this.context, this.activityRecognitionClient)

    private val nullStillDetectorBroadcastReceiver: ActivityDetectorBroadcastReceiverBase = NullActivityDetectorBroadcastReceiver()

    public override fun createActivityDetector(): ActivityDetectorBroadcastReceiverBase {
        if  (!BuildConfig.ENABLE_STILLDETECTION || !this.appSettings.enableAutoPauseOnStill) {
            return this.nullStillDetectorBroadcastReceiver
        }

        return this.stillDetectorBroadcastReceiver
    }
}