package com.janhafner.myskatemap.apps.trackrecorder.views.activities.userprofilesettings

import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.IServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder

internal final class UserProfileSettingsFragmentPresenter(private val view: UserProfileSettingsFragment,
                                                          private val trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>) {
    init {
        this.view.preferenceManager.setSharedPreferencesName("userprofilesettings");

        this.view.addPreferencesFromResource(R.xml.userprofile_settings)
    }

    public fun onViewCreated() {
    }
}