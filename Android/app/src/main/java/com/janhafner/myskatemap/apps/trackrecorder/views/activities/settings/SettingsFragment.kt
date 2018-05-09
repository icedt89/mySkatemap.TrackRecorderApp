package com.janhafner.myskatemap.apps.trackrecorder.views.activities.settings

import android.os.Bundle
import android.preference.ListPreference
import android.preference.PreferenceFragment
import android.view.View
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.getApplicationInjector
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.ServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import io.reactivex.disposables.Disposable
import javax.inject.Inject

internal final class SettingsFragment : PreferenceFragment() {
    @Inject
    public lateinit var trackRecorderServiceController: ServiceController<TrackRecorderServiceBinder>

    private lateinit var trackRecorderServiceControllerSubscription: Disposable

    public override fun onCreate(savedInstanceState: Bundle?) {
        this.context.getApplicationInjector().inject(this)

        super.onCreate(savedInstanceState)

        this.addPreferencesFromResource(R.xml.settings)
    }

    public override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.trackRecorderServiceControllerSubscription = this.trackRecorderServiceController.startAndBindService().subscribe{
            if(it) {
                val binder = this.trackRecorderServiceController.currentBinder!!

                val locationProviderPreference = this.findPreference("preference_tracking_location_provider") as ListPreference
                locationProviderPreference.isEnabled = binder.currentSession == null

                if(!locationProviderPreference.isEnabled) {
                    locationProviderPreference.setSummary("Deaktiviert")
                }
            }
        }
    }
}