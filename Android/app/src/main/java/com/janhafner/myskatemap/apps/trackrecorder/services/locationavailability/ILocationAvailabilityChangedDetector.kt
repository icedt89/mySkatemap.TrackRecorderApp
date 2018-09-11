package com.janhafner.myskatemap.apps.trackrecorder.services.locationavailability

import io.reactivex.Observable

internal interface ILocationAvailabilityChangedDetector {
    val locationAvailabilityChanged: Observable<Boolean>
}