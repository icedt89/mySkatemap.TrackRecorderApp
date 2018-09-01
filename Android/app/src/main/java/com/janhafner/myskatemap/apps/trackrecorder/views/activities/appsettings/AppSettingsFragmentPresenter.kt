package com.janhafner.myskatemap.apps.trackrecorder.views.activities.appsettings

import com.janhafner.myskatemap.apps.trackrecorder.R

internal final class AppSettingsFragmentPresenter(private val view: AppSettingsFragment) {
    init {
        this.view.preferenceManager.setSharedPreferencesName("appsettings");

        this.view.addPreferencesFromResource(R.xml.app_settings)
    }
}