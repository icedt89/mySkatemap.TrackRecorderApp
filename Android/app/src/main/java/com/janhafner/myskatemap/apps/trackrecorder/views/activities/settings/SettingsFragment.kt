package com.janhafner.myskatemap.apps.trackrecorder.views.activities.settings

import android.os.Bundle
import android.preference.PreferenceFragment
import com.janhafner.myskatemap.apps.trackrecorder.R

internal final class SettingsFragment : PreferenceFragment() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.addPreferencesFromResource(R.xml.settings)
    }
}