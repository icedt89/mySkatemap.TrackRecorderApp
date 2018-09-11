package com.janhafner.myskatemap.apps.trackrecorder.views.activities.userprofilesettings

import com.janhafner.myskatemap.apps.trackrecorder.R

internal final class UserProfileSettingsFragmentPresenter(private val view: UserProfileSettingsFragment) {
    init {
        this.view.preferenceManager.setSharedPreferencesName("userprofilesettings");

        this.view.addPreferencesFromResource(R.xml.userprofile_settings)
    }
}