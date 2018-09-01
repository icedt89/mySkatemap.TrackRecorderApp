package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import com.janhafner.myskatemap.apps.trackrecorder.toLocation

internal final class LegacyLocationProvider(private val context: Context,
                                            private val locationManager: LocationManager): LocationProvider() {
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

        this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2500, 5f, this.locationListener, null)

        this.isActive = true
    }

    public override fun stopLocationUpdates() {
        if(this.isDestroyed) {
            throw IllegalStateException("Object is destroyed!")
        }

        if (!this.isActive) {
            throw IllegalStateException("LocationProvider must be started first!")
        }

        this.locationManager.removeUpdates(this.locationListener)

        this.isActive = false
    }

    public override fun getlastKnownLocation(): com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.data.Location? {
        if(this.isDestroyed) {
            throw IllegalStateException("Object is destroyed!")
        }

        if(this.context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            throw IllegalStateException("ACCESS_FINE_LOCATION must be granted!")
        }

        val lastKnownLocation = this.locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        if(lastKnownLocation != null) {
            return lastKnownLocation.toLocation(-1)
        }

        return null
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