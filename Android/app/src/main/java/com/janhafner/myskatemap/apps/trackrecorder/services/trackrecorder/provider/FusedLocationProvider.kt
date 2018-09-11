package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.tasks.Tasks
import com.janhafner.myskatemap.apps.trackrecorder.BuildConfig
import com.janhafner.myskatemap.apps.trackrecorder.services.models.Location
import com.janhafner.myskatemap.apps.trackrecorder.services.toLocation
import java.util.concurrent.ExecutionException

internal final class FusedLocationProvider(private val context: Context,
                                           private val fusedLocationProviderClient: FusedLocationProviderClient): LocationProvider() {
    private val locationCallback: LocationCallback = object: LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val self = this@FusedLocationProvider

            for(sourceLocation in locationResult.locations) {
                val location = sourceLocation.toLocation()

                self.publishLocationUpdate(location)
            }
        }
    }

    private val locationRequest: LocationRequest = LocationRequest.create()

    init {
        locationRequest.fastestInterval = BuildConfig.FUSED_LOCATION_PROVIDER_FASTEST_INTERVAL_IN_MILLISECONDS.toLong()
        locationRequest.interval = BuildConfig.FUSED_LOCATION_PROVIDER_INTERVAL_IN_MILLISECONDS.toLong()
        locationRequest.maxWaitTime = BuildConfig.FUSED_LOCATION_PROVIDER_MAX_WAIT_TIME_IN_MILLISECONDS.toLong()
        locationRequest.smallestDisplacement = BuildConfig.FUSED_LOCATION_PROVIDER_SMALLEST_DISPLACEMENT_METERS
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    public override fun startLocationUpdates() {
        if(this.isDestroyed) {
            throw IllegalStateException("Object is destroyed!")
        }

        if (this.isActive) {
            throw IllegalStateException("LocationProvider must be stopped first!")
        }

        if(this.context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            throw IllegalStateException("ACCESS_FINE_LOCATION must be granted!")
        }

        this.fusedLocationProviderClient.requestLocationUpdates(this.locationRequest, this.locationCallback, null)

        this.isActive = true
    }

    public override fun stopLocationUpdates() {
        if(this.isDestroyed) {
            throw IllegalStateException("Object is destroyed!")
        }

        if (!this.isActive) {
            throw IllegalStateException("LocationProvider must be started first!")
        }

        this.fusedLocationProviderClient.removeLocationUpdates(this.locationCallback)

        this.isActive = false
    }

    public override fun getLastKnownLocation(): Location? {
        if(this.isDestroyed) {
            throw IllegalStateException("Object is destroyed!")
        }

        if(this.context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            throw IllegalStateException("ACCESS_FINE_LOCATION must be granted!")
        }

        try {
            val result = Tasks.await(this.fusedLocationProviderClient.lastLocation)

            return result.toLocation()
        } catch(exception: ExecutionException) {
            return null
        }
    }

    protected final override fun destroyCore() {
        if(this.isDestroyed) {
            return
        }

        if(this.isActive) {
            this.stopLocationUpdates()
        }
    }
}