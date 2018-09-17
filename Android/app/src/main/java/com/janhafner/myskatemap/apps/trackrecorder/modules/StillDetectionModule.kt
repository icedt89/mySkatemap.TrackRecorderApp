package com.janhafner.myskatemap.apps.trackrecorder.modules

import android.content.Context
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityRecognitionClient
import com.janhafner.myskatemap.apps.trackrecorder.BuildConfig
import com.janhafner.myskatemap.apps.trackrecorder.stilldetection.IStillDetector
import com.janhafner.myskatemap.apps.trackrecorder.stilldetection.MostProbableStillDetectorBroadcastReceiver
import com.janhafner.myskatemap.apps.trackrecorder.stilldetection.NullStillDetectorBroadcastReceiver
import com.janhafner.myskatemap.apps.trackrecorder.stilldetection.TransitioningStillDetectorBroadcastReceiver
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
internal final class StillDetectionModule() {
    @Singleton
    @Provides
    public fun provideActivityRecognitionClient(context: Context): ActivityRecognitionClient {
        return ActivityRecognition.getClient(context)
    }
    @Singleton
    @Provides
    public fun provideStillDetector(context: Context, activityRecognitionClient: ActivityRecognitionClient) : IStillDetector {
        if (BuildConfig.STILL_DETECTION_ENABLE) {
            if (BuildConfig.STILL_DETECTION_USE_TRANSITIONS) {
                return TransitioningStillDetectorBroadcastReceiver(context, activityRecognitionClient)
            }

            return MostProbableStillDetectorBroadcastReceiver(context, BuildConfig.STILL_DETECTION_DETECTION_INTERVAL_IN_MILLISECONDS, activityRecognitionClient)
        }

        return NullStillDetectorBroadcastReceiver()
    }
}