package com.janhafner.myskatemap.apps.trackrecorder.location

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.*
import com.janhafner.myskatemap.apps.trackrecorder.toLocation

internal final class FusedLocationProvider : LocationProvider {
    private final val fusedLocationProviderClient: FusedLocationProviderClient;

    private final val locationCallback: LocationCallback;

    private final val locationRequest: LocationRequest;

    public constructor(context: Context) : super() {
        if (context == null) {
            throw IllegalArgumentException("context");
        }

        this.locationRequest = LocationRequest();
        locationRequest.interval = 8000;
        locationRequest.fastestInterval = 4000;
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY;

        this.fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);

        this.locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)

                for (receivedLocation in locationResult.locations) {
                    val location = receivedLocation.toLocation();

                    this@FusedLocationProvider.PostLocationUpdate(location);
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    public final override fun startLocationUpdates() {
        if (this.hasRequestedLocationUpdates) {
            return;
        }

        this.fusedLocationProviderClient.requestLocationUpdates(this.locationRequest, this.locationCallback, null);

        this.hasRequestedLocationUpdates = true;
    }

    public final override fun stopLocationUpdates() {
        if (!this.hasRequestedLocationUpdates) {
            return;
        }

        this.fusedLocationProviderClient.removeLocationUpdates(this.locationCallback);

        this.hasRequestedLocationUpdates = false;
    }
}