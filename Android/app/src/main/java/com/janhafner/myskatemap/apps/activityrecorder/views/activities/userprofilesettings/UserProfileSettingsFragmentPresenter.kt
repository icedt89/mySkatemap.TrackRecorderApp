package com.janhafner.myskatemap.apps.activityrecorder.views.activities.userprofilesettings

import android.content.SharedPreferences
import com.janhafner.myskatemap.apps.activityrecorder.R
import com.janhafner.myskatemap.apps.activityrecorder.settings.FixedEditTextPreference

internal final class UserProfileSettingsFragmentPresenter(private val view: UserProfileSettingsFragment)
        : SharedPreferences.OnSharedPreferenceChangeListener {
    init {
        this.view.preferenceManager.sharedPreferencesName = "userprofilesettings"

        this.view.addPreferencesFromResource(R.xml.userprofile_settings)

        this.view.preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    public override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        val preference = this.view.findPreference(key)
        if(preference != null && preference is FixedEditTextPreference) {
            preference.invalidateSummary()
        }
    }

    public fun onDestroy() {
        this.view.preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }
}