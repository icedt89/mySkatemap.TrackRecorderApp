package com.janhafner.myskatemap.apps.trackrecorder.location

import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.ObservableSubscription
import java.util.*

internal interface ITrackRecorderService {
    fun startLocationTracking();

    fun stopLocationTracking();

    fun deleteAllLocations();

    val locations: Iterable<Location>;

    fun addIsActiveObserver(observer: Observer) : ObservableSubscription;
}