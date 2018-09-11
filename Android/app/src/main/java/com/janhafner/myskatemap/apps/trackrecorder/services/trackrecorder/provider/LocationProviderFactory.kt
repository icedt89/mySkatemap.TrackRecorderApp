package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider

import android.content.Context
import android.location.LocationManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.janhafner.myskatemap.apps.trackrecorder.BuildConfig
import com.janhafner.myskatemap.apps.trackrecorder.services.models.Location
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
            val initialLocation = Location()

            initialLocation.bearing = BuildConfig.SIMULATED_LOCATION_PROVIDER_INITIAL_BEARING
            initialLocation.latitude = BuildConfig.MAP_INITIAL_LATITUDE
            initialLocation.longitude = BuildConfig.MAP_INITIAL_LONGITUDE

            return SimulatedLocationProvider(this.context, initialLocation,
                    BuildConfig.SIMULATED_LOCATION_PROVIDER_BEARING_STEPPING,
                    BuildConfig.SIMULATED_LOCATION_PROVIDER_LATITUDE_STEPPING,
                    BuildConfig.SIMULATED_LOCATION_PROVIDER_LONGITUDE_STEPPING,
                    BuildConfig.SIMULATED_LOCATION_PROVIDER_DELAY_IN_MILLISECONDS,
                    BuildConfig.SIMULATED_LOCATION_PROVIDER_INTERVAL_IN_MILLISECONDS,
                    BuildConfig.SIMULATED_LOCATION_PROVIDER_FORCE_NEED_OF_LOCATION_SERVICES)
        }

        return FusedLocationProvider(this.context, this.fusedLocationProviderClient)
    }
}