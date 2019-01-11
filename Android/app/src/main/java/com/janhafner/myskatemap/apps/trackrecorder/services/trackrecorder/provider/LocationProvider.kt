package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider

import com.janhafner.myskatemap.apps.trackrecorder.common.types.Location
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject

internal abstract class LocationProvider: ILocationProvider {
    private val locationReceivedSubject: Subject<Location> = PublishSubject.create<Location>()
    public final override val locationsReceived: io.reactivex.Observable<Location> = this.locationReceivedSubject

    public override var isActive: Boolean = false
        protected set

    protected fun publishLocationUpdate(location: Location) {
        this.locationReceivedSubject.onNext(location)
    }

    protected var isDestroyed: Boolean = false
    public override fun destroy() {
        if(this.isDestroyed) {
            return
        }

        this.destroyCore()

        this.locationReceivedSubject.onComplete()

        this.isDestroyed = true
    }

    protected abstract fun destroyCore()
}