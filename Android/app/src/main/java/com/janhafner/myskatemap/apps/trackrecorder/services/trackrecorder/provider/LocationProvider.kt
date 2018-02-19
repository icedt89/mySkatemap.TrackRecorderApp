package com.janhafner.myskatemap.apps.trackrecorder.location.provider

import com.janhafner.myskatemap.apps.trackrecorder.location.Location
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject

internal abstract class LocationProvider: ILocationProvider {
    protected var currentSequenceNumber: Int = -1
        private set

    private val locationsSubject: Subject<Location> = PublishSubject.create<Location>()
    public final override val locations: io.reactivex.Observable<Location> = this.locationsSubject

    public override var isActive: Boolean = false
        protected set

    protected fun postLocationUpdate(location: Location) {
        this.locationsSubject.onNext(location)
    }

    protected fun generateSequenceNumber(): Int {
        return ++this.currentSequenceNumber
    }

    public override fun resetSequenceNumber() {
        this.overrideSequenceNumber(-1)
    }

    public override fun overrideSequenceNumber(sequenceNumber: Int) {
        if (this.isActive) {
            throw IllegalStateException()
        }

        if (sequenceNumber < -1) {
            throw IllegalArgumentException("sequenceNumber")
        }

        this.currentSequenceNumber = sequenceNumber
    }
}