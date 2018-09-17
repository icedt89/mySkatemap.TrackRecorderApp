package com.janhafner.myskatemap.apps.trackrecorder.views.activities.appsettings

import com.janhafner.myskatemap.apps.trackrecorder.BuildConfig
import com.janhafner.myskatemap.apps.trackrecorder.R

internal final class AppSettingsFragmentPresenter(private val view: AppSettingsFragment) {
    init {
        this.view.preferenceManager.setSharedPreferencesName("appsettings");

        this.view.addPreferencesFromResource(R.xml.app_settings)
    }

    public fun onViewCreated() {
        val enableAutoPauseOnStillPreference = this.view.findPreference(this.view.getString(R.string.appsettings_preference_enable_auto_pause_on_still_key))
        enableAutoPauseOnStillPreference.isEnabled = BuildConfig.STILL_DETECTION_ENABLE
    }
}