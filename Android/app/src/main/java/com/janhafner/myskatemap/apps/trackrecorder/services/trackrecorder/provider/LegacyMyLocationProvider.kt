package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import com.janhafner.myskatemap.apps.trackrecorder.common.IDestroyable
import com.janhafner.myskatemap.apps.trackrecorder.common.Optional
import com.janhafner.myskatemap.apps.trackrecorder.common.toLocation
import com.janhafner.myskatemap.apps.trackrecorder.common.types.Location
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.reactivestreams.Subscriber

internal final class LegacyMyLocationProvider(private val context: Context,
                                              private val locationManager: LocationManager) : IMyLocationProvider {

    public override fun getMyCurrentLocation(): IMyLocationRequestState {
        if(this.context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            throw IllegalStateException("ACCESS_FINE_LOCATION must be granted!")
        }

        val locationListener = MyLocationListener()
        val location = Single.fromPublisher<Optional<Location>> {
            publisher ->
                locationListener.publisher = publisher

            this.locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER,
                    locationListener,
                    Looper.getMainLooper())
        }.subscribeOn(Schedulers.computation())

        return MyLocationRequestState(location, locationListener, this.locationManager)
    }

    private final class MyLocationListener : LocationListener {
        public var publisher: Subscriber<in Optional<Location>>? = null

        public override fun onLocationChanged(location: android.location.Location?) {
            if(this.publisher == null) {
                return
            }

            if(location != null) {
                this.publisher!!.onNext(Optional(location.toLocation()))
            } else {
                this.publisher!!.onNext(Optional(null))
            }

            this.publisher!!.onComplete()
        }

        public override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        }

        public override fun onProviderEnabled(provider: String?) {
        }

        public override fun onProviderDisabled(provider: String?) {
        }
    }

    private final class MyLocationRequestState(
            public override val location: Single<Optional<Location>>,
            private val locationListener: LocationListener,
            private val locationManager: LocationManager) : IMyLocationRequestState, IDestroyable {
        private var isDestroyed = false
        public override fun destroy() {
            if(this.isDestroyed) {
                return
            }

            this.locationManager.removeUpdates(this.locationListener)

            this.isDestroyed = true
        }

        public override fun cancel() {
            this.destroy()
        }
    }
}
