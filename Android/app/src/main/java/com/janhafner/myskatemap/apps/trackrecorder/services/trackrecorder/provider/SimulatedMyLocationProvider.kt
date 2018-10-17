package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider

import android.os.SystemClock
import com.janhafner.myskatemap.apps.trackrecorder.common.Optional
import com.janhafner.myskatemap.apps.trackrecorder.common.types.Location
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.ThreadLocalRandom

internal final class SimulatedMyLocationProvider : IMyLocationProvider {

    public override fun getMyCurrentLocation(): IMyLocationRequestState {
        val location = Single.fromCallable {
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

        return MyLocationRequestState(location)
    }

    private final class MyLocationRequestState(public override val location: Single<Optional<Location>>) : IMyLocationRequestState {
        public override fun cancel() {
        }
    }
}
