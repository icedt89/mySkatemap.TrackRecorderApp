package com.janhafner.myskatemap.apps.trackrecorder.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.janhafner.myskatemap.apps.trackrecorder.isLocationServicesEnabled
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject


internal final class LocationAvailabilityChangedBroadcastReceiver(context: Context): BroadcastReceiver() {
    private val locationAvailabilityChangedSubject: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(context.isLocationServicesEnabled())
    public val locationAvailabilityChanged: Observable<Boolean> = this.locationAvailabilityChangedSubject

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) {
            throw IllegalArgumentException("context")
        }

        if (intent == null) {
            throw IllegalArgumentException("intent")
        }

        if (intent.action != LocationAvailabilityChangedBroadcastReceiver.PROVIDERS_CHANGED) {
            return
        }

        val isLocationModeEnabled = context.isLocationServicesEnabled()

        this.locationAvailabilityChangedSubject.onNext(isLocationModeEnabled)
    }

    companion object {
        public val PROVIDERS_CHANGED: String = "android.location.PROVIDERS_CHANGED"
    }
}

