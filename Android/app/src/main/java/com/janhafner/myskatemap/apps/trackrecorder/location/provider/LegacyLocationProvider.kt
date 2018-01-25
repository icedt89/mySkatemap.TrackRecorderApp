package com.janhafner.myskatemap.apps.trackrecorder.location.provider

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import com.janhafner.myskatemap.apps.trackrecorder.toLocation

internal final class LegacyLocationProvider(context : Context) : LocationProvider() {
    private val locationManager : LocationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(receivedLocation: Location?) {
            if(receivedLocation == null) {
                return
            }

            val self = this@LegacyLocationProvider

            val sequenceNumber = self.generateSequenceNumber()

            val location = receivedLocation.toLocation(sequenceNumber)

            self.postLocationUpdate(location)
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        }

        override fun onProviderEnabled(provider: String?) {
        }

        override fun onProviderDisabled(provider: String?) {
        }
    }

    @SuppressLint("MissingPermission")
    override fun startLocationUpdates() {
        if (this.isActive) {
            throw IllegalStateException()
        }

        this.locationManager.requestLocationUpdates("gps", 2500, 5f, this.locationListener, null)

        this.isActive = true
    }

    override fun stopLocationUpdates() {
        if (!this.isActive) {
            throw IllegalStateException()
        }

        this.locationManager.removeUpdates(this.locationListener)

        this.isActive = false
    }
}