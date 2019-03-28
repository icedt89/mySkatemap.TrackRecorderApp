package com.janhafner.myskatemap.apps.activityrecorder.views.activities.appsettings

import com.janhafner.myskatemap.apps.activityrecorder.BuildConfig
import com.janhafner.myskatemap.apps.activityrecorder.R

internal final class AppSettingsFragmentPresenter(private val view: AppSettingsFragment) {
    init {
        this.view.preferenceManager.sharedPreferencesName = "appsettings"

        this.view.addPreferencesFromResource(R.xml.app_settings)
    }

    public fun onViewCreated() {
        val enableAutoPauseOnStillPreference = this.view.findPreference(this.view.getString(R.string.appsettings_preference_enable_auto_pause_on_still_key))
        enableAutoPauseOnStillPreference.isEnabled = BuildConfig.STILL_DETECTION_ENABLE

        val enableLiveLocationKey = this.view.findPreference(this.view.getString(R.string.appsettings_preference_enable_live_location_key))
        enableLiveLocationKey.isEnabled = BuildConfig.LIVE_LOCATION_ENABLE
    }
}