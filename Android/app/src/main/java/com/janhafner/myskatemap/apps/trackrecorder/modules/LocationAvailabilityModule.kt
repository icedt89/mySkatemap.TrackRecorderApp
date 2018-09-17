package com.janhafner.myskatemap.apps.trackrecorder.modules

import android.content.Context
import com.janhafner.myskatemap.apps.trackrecorder.services.locationavailability.LocationAvailabilityChangedBroadcastReceiver
import dagger.Module
import dagger.Provides

@Module
internal final class LocationAvailabilityModule() {
    @Provides
    public fun provideLocationAvailabilityChangedBroadcastReceiver(context: Context): LocationAvailabilityChangedBroadcastReceiver {
        return LocationAvailabilityChangedBroadcastReceiver(context)
    }
}