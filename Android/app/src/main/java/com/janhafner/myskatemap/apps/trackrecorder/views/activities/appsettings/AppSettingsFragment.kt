package com.janhafner.myskatemap.apps.trackrecorder.views.activities.appsettings

import android.os.Bundle
import android.preference.PreferenceFragment


internal final class AppSettingsFragment : PreferenceFragment() {
    private lateinit var presenter: AppSettingsFragmentPresenter

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.presenter = AppSettingsFragmentPresenter(this)
    }
}