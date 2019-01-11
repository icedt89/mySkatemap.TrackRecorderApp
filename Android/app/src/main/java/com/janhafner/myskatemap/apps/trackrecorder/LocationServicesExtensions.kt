package com.janhafner.myskatemap.apps.trackrecorder

import android.content.Context
import android.content.IntentFilter
import android.location.LocationManager
import com.github.karczews.rxbroadcastreceiver.RxBroadcastReceivers
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import io.reactivex.Observable
import io.reactivex.Single

public fun Context.isLocationServicesEnabled(): Single<Boolean> {
    return Single.create {
        singleEmitter ->
        val settingsClient = LocationServices.getSettingsClient(this)

        val locationRequest = LocationRequest.create().withDefaultBuildConfig()

        val locationSettingsRequest = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .build()

        val response = settingsClient.checkLocationSettings(locationSettingsRequest)

        response.addOnCompleteListener {
            try {
                val result = it.getResult(ApiException::class.java)!!

                singleEmitter.onSuccess(result.locationSettingsStates.isGpsPresent &&
                        result.locationSettingsStates.isGpsUsable &&
                        result.locationSettingsStates.isLocationPresent &&
                        result.locationSettingsStates.isLocationUsable &&
                        result.locationSettingsStates.isNetworkLocationPresent &&
                        result.locationSettingsStates.isNetworkLocationUsable)
            } catch (exception: ApiException) {
                singleEmitter.onError(exception)
            }
        }
    }
}

public fun Context.locationServicesAvailabilityChanged(): Observable<Boolean> {
    return RxBroadcastReceivers.fromIntentFilter(this, IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION))
            .flatMapSingle {
                this.isLocationServicesEnabled()
                        .onErrorReturn {
                            false
                        }
            }
            .startWith(this.isLocationServicesEnabled()
                    .onErrorReturn {
                        false
                    }.toObservable())
            .replay(1)
            .autoConnect()
}