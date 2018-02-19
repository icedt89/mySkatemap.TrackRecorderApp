package com.janhafner.myskatemap.apps.trackrecorder.location.provider

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.*
import com.janhafner.myskatemap.apps.trackrecorder.location.Location
import com.janhafner.myskatemap.apps.trackrecorder.toLocation

internal final class FusedLocationProvider(context: Context): LocationProvider() {
    private val fusedLocationProviderClient: FusedLocationProviderClient

    private val locationCallback: LocationCallback = object: LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val self = this@FusedLocationProvider

            for(sourceLocation in locationResult.locations) {
                val sequenceNumber = self.generateSequenceNumber()

                val location = sourceLocation.toLocation(sequenceNumber)

                self.postLocationUpdate(location)
            }
        }
    }

    private val locationRequest: LocationRequest = LocationRequest()

    init {
        locationRequest.interval = 8000
        locationRequest.fastestInterval = 4000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        this.fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    }

    @SuppressLint("MissingPermission")
    public override fun startLocationUpdates() {
        if (this.isActive) {
            throw IllegalStateException()
        }

        this.fusedLocationProviderClient.requestLocationUpdates(this.locationRequest, this.locationCallback, null)

        this.isActive = true
    }

    public override fun stopLocationUpdates() {
        if (!this.isActive) {
            throw IllegalStateException()
        }

        this.fusedLocationProviderClient.removeLocationUpdates(this.locationCallback)

        this.isActive = false
    }

    @SuppressLint("MissingPermission")
    public override fun getCurrentLocation(): Location? {
        return this.fusedLocationProviderClient.lastLocation.result.toLocation(-1)
    }
}