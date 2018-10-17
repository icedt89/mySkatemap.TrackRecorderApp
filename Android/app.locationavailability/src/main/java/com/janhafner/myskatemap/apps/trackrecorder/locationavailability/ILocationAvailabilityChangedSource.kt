package com.janhafner.myskatemap.apps.trackrecorder.locationavailability

import io.reactivex.Observable

public interface ILocationAvailabilityChangedSource {
    val locationAvailable: Observable<Boolean>
}