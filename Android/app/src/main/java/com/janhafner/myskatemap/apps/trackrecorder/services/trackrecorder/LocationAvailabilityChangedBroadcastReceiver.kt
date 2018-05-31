package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import com.janhafner.myskatemap.apps.trackrecorder.isLocationServicesEnabled
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject


internal final class LocationAvailabilityChangedBroadcastReceiver(context: Context): BroadcastReceiver() {
    private val locationAvailabilityChangedSubject: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(context.isLocationServicesEnabled())
    public val locationAvailabilityChanged: Observable<Boolean> = this.locationAvailabilityChangedSubject

    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent == null || intent.action != LocationManager.PROVIDERS_CHANGED_ACTION) {
            return
        }

        val isLocationModeEnabled = context!!.isLocationServicesEnabled()

        this.locationAvailabilityChangedSubject.onNext(isLocationModeEnabled)
    }
}

