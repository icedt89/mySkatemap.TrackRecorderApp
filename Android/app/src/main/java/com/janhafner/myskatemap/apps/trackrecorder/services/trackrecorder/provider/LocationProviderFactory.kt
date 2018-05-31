package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider

import android.content.Context
import android.location.LocationManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.janhafner.myskatemap.apps.trackrecorder.io.data.Location
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings

internal final class LocationProviderFactory(private val context: Context,
                                             private val appSettings: IAppSettings,
                                             private val fusedLocationProviderClient: FusedLocationProviderClient,
                                             private val locationManager: LocationManager) : ILocationProviderFactory {
    public override fun createLocationProvider(locationProviderTypeName: String?) : ILocationProvider {
        val realLocationProviderTypeName: String
        if(locationProviderTypeName == null) {
            realLocationProviderTypeName = this.appSettings.locationProviderTypeName
        } else {
            realLocationProviderTypeName = locationProviderTypeName
        }

        if (realLocationProviderTypeName == TestLocationProvider::class.java.name) {
            val initialLocation = Location(-1)

            initialLocation.bearing = 1.0f
            initialLocation.latitude = 50.8333
            initialLocation.longitude = 12.9167

            return TestLocationProvider(this.context, initialLocation, interval = 500)
        }

        if (realLocationProviderTypeName == LegacyLocationProvider::class.java.name) {
            return LegacyLocationProvider(this.context, this.locationManager)
        }

        return FusedLocationProvider(this.context, this.fusedLocationProviderClient)
    }
}