package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider

import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.Location
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject

internal abstract class LocationProvider: ILocationProvider {
    private var currentSequenceNumber: Int = -1

    private val locationReceivedSubject: Subject<Location> = PublishSubject.create<Location>()
    public final override val locationsReceived: io.reactivex.Observable<Location> = this.locationReceivedSubject

    private val sequenceNumberOverriddenSubject: Subject<Int> = PublishSubject.create<Int>()
    public final override val sequenceNumberOverridden: io.reactivex.Observable<Int> = this.sequenceNumberOverriddenSubject

    private val activityChangedSubject: Subject<Boolean> = BehaviorSubject.createDefault(false)
    public final override val activityChanged: io.reactivex.Observable<Boolean> = this.activityChangedSubject

    public override var isActive: Boolean = false
        protected set(value) {
            field = value
            this.activityChangedSubject.onNext(value)
        }

    protected fun publishLocationUpdate(location: Location) {
        this.locationReceivedSubject.onNext(location)
    }

    protected fun generateSequenceNumber(): Int {
        return ++this.currentSequenceNumber
    }

    public override fun resetSequenceNumber() {
        this.overrideSequenceNumber(-1)
    }

    public override fun overrideSequenceNumber(sequenceNumber: Int) {
        if (this.isActive) {
            throw IllegalStateException("LocationProvider must be stopped first!")
        }

        if (sequenceNumber < -1) {
            throw IllegalArgumentException("sequenceNumber")
        }

        this.currentSequenceNumber = sequenceNumber
        this.sequenceNumberOverriddenSubject.onNext(sequenceNumber)
    }
}