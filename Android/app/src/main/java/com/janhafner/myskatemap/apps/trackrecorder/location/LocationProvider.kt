package com.janhafner.myskatemap.apps.trackrecorder.location

import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject

internal abstract class LocationProvider : ILocationProvider {
    protected var currentSequenceNumber : Int = -1
        private set

    private val locationObservable: Subject<Location> = PublishSubject.create<Location>()
    public final override val locations: io.reactivex.Observable<Location>
        get() = this.locationObservable

    override var isActive: Boolean = false
        protected set

    protected fun postLocationUpdate(location: Location) {
        this.locationObservable.onNext(location)
    }

    protected fun generateSequenceNumber(): Int {
        return this.currentSequenceNumber++
    }

    public override fun resetSequenceNumber() {
        if(this.isActive) {
            throw IllegalStateException()
        }

        this.currentSequenceNumber = -1
    }

    public override fun overrideSequenceNumber(sequenceNumber : Int) {
        if(this.isActive) {
            throw IllegalStateException()
        }

        if(sequenceNumber < -1) {
            throw IllegalStateException()
        }

        this.currentSequenceNumber = sequenceNumber
    }
}