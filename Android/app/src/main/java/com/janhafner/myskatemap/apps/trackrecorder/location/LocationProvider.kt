package com.janhafner.myskatemap.apps.trackrecorder.location

import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.FixedObservable
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.ObservableSubscription
import io.reactivex.subjects.PublishSubject
import java.util.*

internal abstract class LocationProvider : ILocationProvider {
    private final val locationUpdates: Observable;

    private final val locationObservable: PublishSubject<Location>;

    protected constructor() {
        this.locationUpdates = FixedObservable();

        this.locationObservable = PublishSubject.create<Location>();
    }

    protected final fun PostLocationUpdate(location: Location) {
        if (location == null) {
            throw IllegalArgumentException("location");
        }

        this.locationUpdates.notifyObservers(location);
        this.locationObservable.onNext(location);
    }

    public final override fun addLocationUpdateObserver(observer: Observer) : ObservableSubscription {
        if (observer == null) {
            throw IllegalArgumentException("observer");
        }

        this.locationUpdates.addObserver(observer);

        return ObservableSubscription(this.locationUpdates, observer);
    }

    public override final val locations: io.reactivex.Observable<Location>
        get() = this.locationObservable;

    public final override var hasRequestedLocationUpdates: Boolean = false;
}