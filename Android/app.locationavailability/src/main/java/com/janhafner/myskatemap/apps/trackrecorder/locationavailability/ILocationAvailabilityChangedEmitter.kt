package com.janhafner.myskatemap.apps.trackrecorder.locationavailability

import com.janhafner.myskatemap.apps.trackrecorder.common.IDestroyable

public interface ILocationAvailabilityChangedEmitter : IDestroyable {
    fun emit(isLocationAvailable: Boolean)
}

