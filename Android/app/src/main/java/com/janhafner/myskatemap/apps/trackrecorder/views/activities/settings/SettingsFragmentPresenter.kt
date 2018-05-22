package com.janhafner.myskatemap.apps.trackrecorder.views.activities.settings

import android.preference.ListPreference
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.ServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import io.reactivex.disposables.Disposable

internal final class SettingsFragmentPresenter(private val settingsFragment: SettingsFragment,
                                               private val trackRecorderServiceController: ServiceController<TrackRecorderServiceBinder>) {
    private lateinit var trackRecorderServiceControllerSubscription: Disposable

    init {
        this.settingsFragment.addPreferencesFromResource(R.xml.settings)
    }

    public fun onViewCreated() {
        this.trackRecorderServiceControllerSubscription = this.trackRecorderServiceController.startAndBindService().subscribe{
            if(it) {
                val binder = this.trackRecorderServiceController.currentBinder!!

                val locationProviderPreference = this.settingsFragment.findPreference("preference_tracking_location_provider") as ListPreference
                locationProviderPreference.isEnabled = binder.currentSession == null

                if(!locationProviderPreference.isEnabled) {
                    locationProviderPreference.setSummary("Deaktiviert")
                }
            }
        }
    }
}