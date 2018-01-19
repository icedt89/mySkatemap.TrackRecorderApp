package com.janhafner.myskatemap.apps.trackrecorder.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log


internal final class LocationAvailabilityChangedBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if(context == null) {
            throw IllegalArgumentException("context")
        }

        val contentResolver = context.contentResolver
        val isLocationServicesEnabled = Settings.Secure.getInt(contentResolver, Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF)

        if (isLocationServicesEnabled == Settings.Secure.LOCATION_MODE_OFF) {
            Log.i("LACBR", "DEAKTIVIERT")
        } else {
            Log.i("LACBR", "AKTIVIERT")
        }
    }
}