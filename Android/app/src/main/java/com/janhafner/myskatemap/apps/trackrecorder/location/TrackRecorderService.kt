package com.janhafner.myskatemap.apps.trackrecorder.location

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.FixedObservable
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.ObservableSubscription
import io.reactivex.disposables.Disposable
import java.util.*

internal final class TrackRecorderService : Service, ITrackRecorderService, Observer {
    private final val binder: IBinder;

    private final val LOGTAG: String;

    private final val savedLocations: ArrayList<Location>;

    private final var locationProvider: ILocationProvider?;

    private final var locationUpdateSubscription: ObservableSubscription?;

    private final var locationsSubscription: Disposable?;

    private final val isActiveObservable: FixedObservable;

    public constructor() {
        this.binder = TrackRecorderServiceBinder(this);
        this.LOGTAG = TrackRecorderService::class.java.simpleName;
        this.savedLocations = java.util.ArrayList<Location>();
        this.locationProvider = null;
        this.locationUpdateSubscription = null;
        this.locationsSubscription = null;
        this.isActiveObservable = FixedObservable();
    }

    public final override fun startLocationTracking() {
        this.locationProvider!!.startLocationUpdates();

        if(this.locationProvider!!.hasRequestedLocationUpdates) {
            this.isActiveObservable.notifyObservers(true);
        }
    }

    public final override fun stopLocationTracking() {
        this.locationProvider!!.stopLocationUpdates();

        if(!this.locationProvider!!.hasRequestedLocationUpdates) {
            this.isActiveObservable.notifyObservers(false);
        }
    }

    public final override fun deleteAllLocations() {
        this.savedLocations.clear();
    }

    public final override val locations: Iterable<Location>
        get() = this.savedLocations;

    public override fun onBind(intent: Intent?): IBinder {
        return this.binder;
    }

    public final override fun onCreate() {
        super.onCreate();

        this.locationProvider = FusedLocationProvider(this);

        this.locationUpdateSubscription = this.locationProvider!!.addLocationUpdateObserver(this);
        this.locationsSubscription = this.locationProvider!!.locations.subscribe({location: Location? -> {
            if(location == null) {
                throw IllegalArgumentException("arg");
            }

            Log.i(this.LOGTAG, String.format("New location received: %s", location));

            this.savedLocations.add(location);
        } })
    }

    public final override fun onDestroy() {
        this.locationUpdateSubscription!!.remove();
        this.locationsSubscription!!.dispose();

        this.isActiveObservable.deleteObservers();

        super.onDestroy()
    }

    override fun addIsActiveObserver(observer: Observer) : ObservableSubscription {
        if(observer == null) {
            throw IllegalArgumentException("observer");
        }

        this.isActiveObservable.addObserver(observer);

        return ObservableSubscription(this.isActiveObservable, observer);
    }

    public final override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY;
    }

    public final override fun update(o: Observable?, arg: Any?) {
        if(arg == null) {
            throw IllegalArgumentException("arg");
        }

        val location = arg as Location;

        Log.i(this.LOGTAG, String.format("New location received: %s", location));

        this.savedLocations.add(location);
    }
}