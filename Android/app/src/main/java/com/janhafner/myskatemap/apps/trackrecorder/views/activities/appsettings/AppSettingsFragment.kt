package com.janhafner.myskatemap.apps.trackrecorder.views.activities.appsettings

import android.os.Bundle
import android.preference.PreferenceFragment
import android.view.View


internal final class AppSettingsFragment : PreferenceFragment() {
    private lateinit var presenter: AppSettingsFragmentPresenter

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.presenter = AppSettingsFragmentPresenter(this)
    }

    public override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.presenter.onViewCreated()
    }
}