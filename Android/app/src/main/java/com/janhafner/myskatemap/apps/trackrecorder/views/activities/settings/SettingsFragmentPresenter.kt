package com.janhafner.myskatemap.apps.trackrecorder.views.activities.settings

import android.content.Context
import android.preference.ListPreference
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.IServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import io.reactivex.disposables.Disposable

internal final class SettingsFragmentPresenter(private val view: SettingsFragment,
                                               private val trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>) {
    private lateinit var trackRecorderServiceControllerSubscription: Disposable

    init {
        this.view.preferenceManager.setSharedPreferencesName("user_profile_settings");
        this.view.preferenceManager.setSharedPreferencesMode(Context.MODE_WORLD_READABLE);

        this.view.addPreferencesFromResource(R.xml.settings)
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