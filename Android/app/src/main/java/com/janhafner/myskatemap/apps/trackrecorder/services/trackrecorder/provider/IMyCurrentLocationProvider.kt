package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.os.SystemClock
import com.janhafner.myskatemap.apps.trackrecorder.common.Optional
import com.janhafner.myskatemap.apps.trackrecorder.services.models.Location
import com.janhafner.myskatemap.apps.trackrecorder.services.toLocation
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.ThreadLocalRandom

internal interface IMyCurrentLocationProvider {
    fun getMyCurrentLocation(): Single<Optional<Location>>
}

internal final class LocationManagerMyCurrentLocationProvider(private val context: Context,
                                                    private val locationManager: LocationManager) : IMyCurrentLocationProvider {
    public override fun getMyCurrentLocation(): Single<Optional<Location>> {
        if(this.context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            throw IllegalStateException("ACCESS_FINE_LOCATION must be granted!")
        }

        return Single.fromPublisher<Optional<Location>> {
            publisher ->
            this.locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, object : LocationListener {
                public override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                }

                public override fun onProviderEnabled(provider: String?) {
                }

                public override fun onProviderDisabled(provider: String?) {
                }

                override fun onLocationChanged(location: android.location.Location?) {
                    if(location != null) {
                        publisher.onNext(Optional(location.toLocation()))
                    } else {
                        publisher.onNext(Optional(null))
                    }

                    publisher.onComplete()
                }
            }, Looper.getMainLooper())
        }.subscribeOn(Schedulers.computation())
    }
}

internal final class SimulatedMyCurrentLocationProvider : IMyCurrentLocationProvider {
    public override fun getMyCurrentLocation(): Single<Optional<Location>> {
        return Single.fromCallable {
            val result = Location()

            result.latitude = ThreadLocalRandom.current().nextDouble() * 50
            if(SystemClock.elapsedRealtimeNanos() % 2 == 0L) {
                result.latitude = -result.latitude
            }

            result.longitude = ThreadLocalRandom.current().nextDouble() * 12
            if(SystemClock.elapsedRealtimeNanos() % 4 == 0L) {
                result.longitude = -result.longitude
            }

            Optional(result)
        }.subscribeOn(Schedulers.computation())
    }
}