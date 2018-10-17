package com.janhafner.myskatemap.apps.trackrecorder.modules

import android.content.Context
import com.janhafner.myskatemap.apps.trackrecorder.locationavailability.ILocationAvailabilityChangedEmitter
import com.janhafner.myskatemap.apps.trackrecorder.locationavailability.ILocationAvailabilityChangedSource
import com.janhafner.myskatemap.apps.trackrecorder.locationavailability.LocationAvailabilityChangedBroadcastReceiver
import com.janhafner.myskatemap.apps.trackrecorder.locationavailability.LocationAvailabilityChangedEmittingSource
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
internal final class LocationAvailabilityModule {
    @Provides
    public fun provideLocationAvailabilityChangedBroadcastReceiver(context: Context, locationAvailabilityChangedEmitter: ILocationAvailabilityChangedEmitter): LocationAvailabilityChangedBroadcastReceiver {
        return LocationAvailabilityChangedBroadcastReceiver(context, locationAvailabilityChangedEmitter)
    }

    @Provides
    @Singleton
    public fun provideLocationAvailabilityChangedEmittingSource() : LocationAvailabilityChangedEmittingSource {
        return LocationAvailabilityChangedEmittingSource()
    }

    @Provides
    @Singleton
    public fun provideLocationAvailabilityChangedEmitter(locationAvailabilityChangedEmittingSource: LocationAvailabilityChangedEmittingSource) : ILocationAvailabilityChangedEmitter {
        return locationAvailabilityChangedEmittingSource
    }

    @Provides
    @Singleton
    public fun provideLocationAvailabilityChangedSource(locationAvailabilityChangedEmittingSource: LocationAvailabilityChangedEmittingSource) : ILocationAvailabilityChangedSource {
        return locationAvailabilityChangedEmittingSource
    }
}