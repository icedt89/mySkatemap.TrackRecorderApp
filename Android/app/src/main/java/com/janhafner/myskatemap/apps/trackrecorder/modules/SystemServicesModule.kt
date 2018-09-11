package com.janhafner.myskatemap.apps.trackrecorder.modules

import android.app.NotificationManager
import android.content.Context
import android.location.LocationManager
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityRecognitionClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
internal final class SystemServicesModule {
    @Singleton
    @Provides
    public fun provideLocationManager(context: Context) : LocationManager {
        return context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    @Singleton
    @Provides
    public fun provideActivityRecognitionClient(context: Context): ActivityRecognitionClient {
        return ActivityRecognition.getClient(context)
    }

    @Singleton
    @Provides
    public fun provideNotificationManager(context: Context) : NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    @Singleton
    @Provides
    public fun provideFusedLocationProviderClient(context: Context) : FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }
}