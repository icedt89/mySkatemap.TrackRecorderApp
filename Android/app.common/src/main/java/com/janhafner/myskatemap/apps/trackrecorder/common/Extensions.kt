package com.janhafner.myskatemap.apps.trackrecorder.common

import android.content.Context
import android.provider.Settings

public fun Context.startLocationServicesSettingsActivity() {
    this.startActivity(android.content.Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
}

public fun Context.isLocationServicesEnabled(): Boolean {
    return Settings.Secure.getInt(this.contentResolver, Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF) != Settings.Secure.LOCATION_MODE_OFF
}