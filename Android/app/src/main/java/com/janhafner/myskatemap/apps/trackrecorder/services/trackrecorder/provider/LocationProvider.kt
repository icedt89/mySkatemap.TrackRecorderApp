package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider

import com.janhafner.myskatemap.apps.trackrecorder.services.models.Location
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject

internal abstract class LocationProvider: ILocationProvider {
    private val locationReceivedSubject: Subject<Location> = PublishSubject.create<Location>()
    public final override val locationsReceived: io.reactivex.Observable<Location> = this.locationReceivedSubject.subscribeOn(Schedulers.computation())

    private val activityChangedSubject: Subject<Boolean> = BehaviorSubject.createDefault(false)
    public final override val activityChanged: io.reactivex.Observable<Boolean> = this.activityChangedSubject.subscribeOn(Schedulers.computation())

    public override var isActive: Boolean = false
        protected set(value) {
            field = value
            this.activityChangedSubject.onNext(value)
        }

    protected fun publishLocationUpdate(location: Location) {
        this.locationReceivedSubject.onNext(location)
    }

    protected var isDestroyed: Boolean = false
    public override fun destroy() {
        if(this.isDestroyed) {
            return
        }

        this.destroyCore()

        this.activityChangedSubject.onComplete()
        this.locationReceivedSubject.onComplete()

        this.isDestroyed = true
    }

    protected abstract fun destroyCore()
}