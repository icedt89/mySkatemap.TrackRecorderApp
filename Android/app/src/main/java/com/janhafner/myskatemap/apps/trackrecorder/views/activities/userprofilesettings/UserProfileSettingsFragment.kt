package com.janhafner.myskatemap.apps.trackrecorder.views.activities.userprofilesettings

import android.os.Bundle
import com.takisoft.preferencex.PreferenceFragmentCompat

internal final class UserProfileSettingsFragment : PreferenceFragmentCompat() {
    private lateinit var presenter: UserProfileSettingsFragmentPresenter

    public override fun onCreatePreferencesFix(savedInstanceState: Bundle?, rootKey: String?) {
        this.presenter = UserProfileSettingsFragmentPresenter(this)
    }

    public override fun onDestroy() {
        super.onDestroy()

        this.presenter.onDestroy()
    }
}