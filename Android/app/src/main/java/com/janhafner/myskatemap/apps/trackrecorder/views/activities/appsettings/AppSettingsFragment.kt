package com.janhafner.myskatemap.apps.trackrecorder.views.activities.appsettings

import android.os.Bundle
import android.view.View
import com.takisoft.preferencex.PreferenceFragmentCompat


internal final class AppSettingsFragment : PreferenceFragmentCompat() {
    private lateinit var presenter: AppSettingsFragmentPresenter

    public override fun onCreatePreferencesFix(savedInstanceState: Bundle?, rootKey: String?) {
        this.presenter = AppSettingsFragmentPresenter(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.presenter.onViewCreated()
    }
}