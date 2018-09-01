package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder

import io.reactivex.Observable

internal interface ILocationAvailabilityChangedDetector {
    val locationAvailabilityChanged: Observable<Boolean>
}