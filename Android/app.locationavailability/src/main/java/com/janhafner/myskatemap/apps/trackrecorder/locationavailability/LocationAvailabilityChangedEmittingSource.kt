package com.janhafner.myskatemap.apps.trackrecorder.locationavailability

import com.janhafner.myskatemap.apps.trackrecorder.common.ObjectDestroyedException
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject

public final class LocationAvailabilityChangedEmittingSource: ILocationAvailabilityChangedEmitter, ILocationAvailabilityChangedSource {
    private val locationAvailableSubject: BehaviorSubject<Boolean> = BehaviorSubject.create()
    public override val locationAvailable: Observable<Boolean> = this.locationAvailableSubject.subscribeOn(Schedulers.computation())
            .distinctUntilChanged()

    public override fun emit(isLocationAvailable: Boolean) {
        if(this.isDestroyed) {
            throw ObjectDestroyedException()
        }

        this.locationAvailableSubject.onNext(isLocationAvailable)
    }

    private var isDestroyed = false
    public override fun destroy() {
        if(this.isDestroyed) {
            return
        }

        this.locationAvailableSubject.onComplete()

        this.isDestroyed = true
    }
}