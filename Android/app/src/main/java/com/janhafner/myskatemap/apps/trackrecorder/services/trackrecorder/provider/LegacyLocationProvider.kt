package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider

import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import com.janhafner.myskatemap.apps.trackrecorder.toLocation

internal final class LegacyLocationProvider(private val locationManager: LocationManager): LocationProvider() {
    private val locationListener = object: LocationListener {
        override fun onLocationChanged(receivedLocation: Location?) {
            if (receivedLocation == null) {
                return
            }

            val self = this@LegacyLocationProvider

            val sequenceNumber = self.generateSequenceNumber()

            val location = receivedLocation.toLocation(sequenceNumber)

            self.publishLocationUpdate(location)
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        }

        override fun onProviderEnabled(provider: String?) {
        }

        override fun onProviderDisabled(provider: String?) {
        }
    }

    @SuppressLint("MissingPermission")
    public override fun startLocationUpdates() {
        if (this.isActive) {
            throw IllegalStateException("LocationProvider must be stopped first!")
        }

        this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2500, 5f, this.locationListener, null)

        this.isActive = true
    }

    public override fun stopLocationUpdates() {
        if (!this.isActive) {
            throw IllegalStateException("LocationProvider must be started first!")
        }

        this.locationManager.removeUpdates(this.locationListener)

        this.isActive = false
    }

    @SuppressLint("MissingPermission")
    public override fun getCurrentLocation(): com.janhafner.myskatemap.apps.trackrecorder.infrastructure.Location {
        return this.locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).toLocation(-1)
    }
}