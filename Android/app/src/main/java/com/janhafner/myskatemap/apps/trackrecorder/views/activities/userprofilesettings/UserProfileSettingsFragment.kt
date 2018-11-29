package com.janhafner.myskatemap.apps.trackrecorder.views.activities.userprofilesettings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat

internal final class UserProfileSettingsFragment : PreferenceFragmentCompat() {
    private lateinit var presenter: UserProfileSettingsFragmentPresenter

    public override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        this.presenter = UserProfileSettingsFragmentPresenter(this)
    }
}