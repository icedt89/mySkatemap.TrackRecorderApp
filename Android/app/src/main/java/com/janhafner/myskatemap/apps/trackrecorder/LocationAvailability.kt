package com.janhafner.myskatemap.apps.trackrecorder

import android.content.Context
import android.content.IntentFilter
import android.location.LocationManager
import com.github.karczews.rxbroadcastreceiver.RxBroadcastReceivers
import com.janhafner.myskatemap.apps.trackrecorder.common.isLocationServicesEnabled
import io.reactivex.Observable

internal class LocationAvailability {
    companion object {
        public fun changed(context: Context): Observable<Boolean> {
            return RxBroadcastReceivers.fromIntentFilter(context, IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION))
                    .map {
                        context.isLocationServicesEnabled()
                    }
                    .startWith(context.isLocationServicesEnabled())
                    .replay(1)
                    .autoConnect()
        }
    }
}