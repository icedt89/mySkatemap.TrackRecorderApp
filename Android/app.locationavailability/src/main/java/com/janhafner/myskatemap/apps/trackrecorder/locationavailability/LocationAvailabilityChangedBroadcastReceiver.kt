package com.janhafner.myskatemap.apps.trackrecorder.locationavailability

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import com.janhafner.myskatemap.apps.trackrecorder.common.isLocationServicesEnabled

public final class LocationAvailabilityChangedBroadcastReceiver(context: Context,
                                                                private val locationAvailabilityChangedEmitter: ILocationAvailabilityChangedEmitter) : BroadcastReceiver() {
    init {
        this.emitCurrentState(context)
    }

    public override fun onReceive(context: Context?, intent: Intent?) {
        if(intent == null || intent.action != LocationManager.PROVIDERS_CHANGED_ACTION) {
            return
        }

        this.emitCurrentState(context!!)
    }

    private fun emitCurrentState(context: Context) {
        val isLocationModeEnabled = context.isLocationServicesEnabled()

        this.locationAvailabilityChangedEmitter.emit(isLocationModeEnabled)
    }
}