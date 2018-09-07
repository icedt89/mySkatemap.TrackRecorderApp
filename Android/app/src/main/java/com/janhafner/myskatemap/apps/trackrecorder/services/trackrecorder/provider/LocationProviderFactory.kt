package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider

import android.content.Context
import android.location.LocationManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.janhafner.myskatemap.apps.trackrecorder.BuildConfig
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.data.Location
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

        if (realLocationProviderTypeName == SimulatedLocationProvider::class.java.simpleName) {
            val initialLocation = Location(-1)

            initialLocation.bearing = BuildConfig.SIMULATED_LOCATION_PROVIDER_INITIAL_BEARING
            initialLocation.latitude = BuildConfig.MAP_INITIAL_LATITUDE
            initialLocation.longitude = BuildConfig.MAP_INITIAL_LONGITUDE

            return SimulatedLocationProvider(this.context, initialLocation,
                    delay = BuildConfig.SIMULATED_LOCATION_PROVIDER_DELAY_IN_MILLISECONDS,
                    interval = BuildConfig.SIMULATED_LOCATION_PROVIDER_INTERVAL_IN_MILLISECONDS)
        }

        if (realLocationProviderTypeName == LegacyLocationProvider::class.java.simpleName) {
            return LegacyLocationProvider(this.context, this.locationManager)
        }

        return FusedLocationProvider(this.context, this.fusedLocationProviderClient)
    }
}