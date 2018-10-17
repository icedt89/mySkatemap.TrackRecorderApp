package com.janhafner.myskatemap.apps.trackrecorder.modules

import android.content.Context
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityRecognitionClient
import com.janhafner.myskatemap.apps.trackrecorder.BuildConfig
import com.janhafner.myskatemap.apps.trackrecorder.activitydetection.ActivityDetectorBroadcastReceiver
import com.janhafner.myskatemap.apps.trackrecorder.activitydetection.ActivityDetectorEmittingSource
import com.janhafner.myskatemap.apps.trackrecorder.activitydetection.IActivityDetectorEmitter
import com.janhafner.myskatemap.apps.trackrecorder.activitydetection.IActivityDetectorSource
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
internal final class ActivityDetectionModule {
    @Provides
    @Singleton
    public fun provideActivityDetectorBroadcastReceiver(context: Context, activityRecognitionClient: ActivityRecognitionClient, activityDetectorEmitter: IActivityDetectorEmitter)
            : ActivityDetectorBroadcastReceiver {
        return ActivityDetectorBroadcastReceiver(
                context,
                BuildConfig.STILL_DETECTION_DETECTION_INTERVAL_IN_MILLISECONDS,
                activityRecognitionClient,
                activityDetectorEmitter)
    }

    @Provides
    @Singleton
    public fun provideActivityDetectorEmittingSource(): ActivityDetectorEmittingSource {
        return ActivityDetectorEmittingSource()
    }

    @Provides
    @Singleton
    public fun provideActivityDetectorEmitter(stillDetectorEmittingSource: ActivityDetectorEmittingSource): IActivityDetectorEmitter {
        return stillDetectorEmittingSource
    }

    @Provides
    @Singleton
    public fun provideActivityDetectorSource(stillDetectorEmittingSource: ActivityDetectorEmittingSource): IActivityDetectorSource {
        return stillDetectorEmittingSource
    }

    @Provides
    @Singleton
    public fun provideActivityRecognitionClient(context: Context): ActivityRecognitionClient {
        return ActivityRecognition.getClient(context)
    }
}