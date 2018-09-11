package com.janhafner.myskatemap.apps.trackrecorder.views.activities.userprofilesettings

import android.os.Bundle
import android.preference.PreferenceFragment
import com.janhafner.myskatemap.apps.trackrecorder.getApplicationInjector

internal final class UserProfileSettingsFragment : PreferenceFragment() {
    private lateinit var presenter: UserProfileSettingsFragmentPresenter

    public override fun onCreate(savedInstanceState: Bundle?) {
        this.context.getApplicationInjector().inject(this)

        super.onCreate(savedInstanceState)

        this.presenter = UserProfileSettingsFragmentPresenter(this)
    }
}