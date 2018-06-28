package com.janhafner.myskatemap.apps.trackrecorder.views.userprofile.settings

import android.preference.ListPreference
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.IServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.views.userprofile.settings.UserProfileSettingsFragment
import io.reactivex.disposables.Disposable

internal final class UserProfileSettingsFragmentPresenter(private val view: UserProfileSettingsFragment,
                                                          private val trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>) {
    private lateinit var trackRecorderServiceControllerSubscription: Disposable

    init {
        this.view.addPreferencesFromResource(R.xml.user_profile_settings)
    }

    public fun onViewCreated() {
        this.trackRecorderServiceControllerSubscription = this.trackRecorderServiceController.startAndBindService().subscribe{
            if(it) {
                val binder = this.trackRecorderServiceController.currentBinder!!

                val locationProviderPreference = this.view.findPreference("preference_tracking_location_provider") as ListPreference
                locationProviderPreference.isEnabled = binder.currentSession == null

                if(!locationProviderPreference.isEnabled) {
                    locationProviderPreference.setSummary("Deaktiviert")
                }
            }
        }
    }
}