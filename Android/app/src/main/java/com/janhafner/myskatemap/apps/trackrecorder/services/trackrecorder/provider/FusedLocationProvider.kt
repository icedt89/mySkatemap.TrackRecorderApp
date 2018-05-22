package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider

import android.annotation.SuppressLint
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.janhafner.myskatemap.apps.trackrecorder.io.data.Location
import com.janhafner.myskatemap.apps.trackrecorder.toLocation

internal final class FusedLocationProvider(private val fusedLocationProviderClient: FusedLocationProviderClient): LocationProvider() {
    private val locationCallback: LocationCallback = object: LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val self = this@FusedLocationProvider

            for(sourceLocation in locationResult.locations) {
                val sequenceNumber = self.generateSequenceNumber()

                val location = sourceLocation.toLocation(sequenceNumber)

                self.publishLocationUpdate(location)
            }
        }
    }

    private val locationRequest: LocationRequest = LocationRequest.create()

    init {
        locationRequest.interval = 8000
        locationRequest.fastestInterval = 4000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    @SuppressLint("MissingPermission")
    public override fun startLocationUpdates() {
        if (this.isActive) {
            throw IllegalStateException("LocationProvider must be stopped first!")
        }

        this.fusedLocationProviderClient.requestLocationUpdates(this.locationRequest, this.locationCallback, null)

        this.isActive = true
    }

    public override fun stopLocationUpdates() {
        if (!this.isActive) {
            throw IllegalStateException("LocationProvider must be started first!")
        }

        this.fusedLocationProviderClient.removeLocationUpdates(this.locationCallback)

        this.isActive = false
    }

    @SuppressLint("MissingPermission")
    public override fun getCurrentLocation(): Location? {
        return this.fusedLocationProviderClient.lastLocation.result.toLocation(-1)
    }
}