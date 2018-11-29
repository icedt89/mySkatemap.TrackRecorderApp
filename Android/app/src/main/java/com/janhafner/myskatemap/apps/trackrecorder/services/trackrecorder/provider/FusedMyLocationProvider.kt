package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import com.google.android.gms.tasks.Task
import com.janhafner.myskatemap.apps.trackrecorder.common.IDestroyable
import com.janhafner.myskatemap.apps.trackrecorder.common.Optional
import com.janhafner.myskatemap.apps.trackrecorder.common.toLocation
import com.janhafner.myskatemap.apps.trackrecorder.common.types.Location
import com.janhafner.myskatemap.apps.trackrecorder.getFusedLocationProviderClient
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

internal final class FusedMyLocationProvider(private val context: Context) : IMyLocationProvider {

    public override fun getMyCurrentLocation(): IMyLocationRequestState {
        if(this.context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            throw IllegalStateException("ACCESS_FINE_LOCATION must be granted!")
        }

        val lastLocationTask = context.getFusedLocationProviderClient().lastLocation

        val location = Single.fromPublisher<Optional<Location>> {
            publisher ->
                lastLocationTask.addOnSuccessListener {
                    publisher.onNext(Optional(it.toLocation()))

                    publisher.onComplete()
                }
                lastLocationTask.addOnFailureListener{
                    publisher.onError(it)
                }
        }.subscribeOn(Schedulers.computation())

        return MyLocationRequestState(location, lastLocationTask)
    }

    private final class MyLocationRequestState(
            public override val location: Single<Optional<Location>>,
            private val lastLocationTask: Task<android.location.Location>) : IMyLocationRequestState, IDestroyable {
        private var isDestroyed = false
        public override fun destroy() {
            if(this.isDestroyed) {
                return
            }

            this.isDestroyed = true
        }

        public override fun cancel() {
            this.destroy()
        }
    }
}
