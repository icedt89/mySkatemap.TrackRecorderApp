package com.janhafner.myskatemap.apps.trackrecorder.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject


internal final class LocationAvailabilityChangedBroadcastReceiver(context : Context) : BroadcastReceiver() {
    private val locationAvailabilityChangedSubject : BehaviorSubject<Boolean> = BehaviorSubject.createDefault(this.isLocationModeEnabled(context))
    public val locationAvailabilityChanged : Observable<Boolean> = this.locationAvailabilityChangedSubject

    override fun onReceive(context: Context?, intent: Intent?) {
        if(context == null) {
            throw IllegalArgumentException("context")
        }

        if(intent == null) {
            throw IllegalArgumentException("intent")
        }

        if(intent!!.action != "android.location.PROVIDERS_CHANGED") {
            return
        }

        val isLocationModeEnabled = this.isLocationModeEnabled(context)

        this.locationAvailabilityChangedSubject.onNext(isLocationModeEnabled)
    }

    private fun isLocationModeEnabled(context : Context) : Boolean {
        val contentResolver = context.contentResolver

        return Settings.Secure.getInt(contentResolver, Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF) != Settings.Secure.LOCATION_MODE_OFF
    }
}