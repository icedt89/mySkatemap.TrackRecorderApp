package com.janhafner.myskatemap.apps.trackrecorder.location

import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.ObservableSubscription
import io.reactivex.Observable
import java.util.*

internal interface ILocationProvider {
    fun startLocationUpdates();

    fun stopLocationUpdates();

    val hasRequestedLocationUpdates: Boolean;

    fun addLocationUpdateObserver(observer: Observer) : ObservableSubscription;

    val locations: Observable<Location>;
}