package com.janhafner.myskatemap.apps.trackrecorder.views.activities.settings

import android.os.Bundle
import android.preference.PreferenceFragment
import android.view.View
import com.janhafner.myskatemap.apps.trackrecorder.getApplicationInjector
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.ServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.refactored.RefactoredTrackRecorderService
import javax.inject.Inject

internal final class SettingsFragment : PreferenceFragment() {
    @Inject
    public lateinit var trackRecorderServiceController: ServiceController<RefactoredTrackRecorderService, TrackRecorderServiceBinder>

    private lateinit var presenter: SettingsFragmentPresenter

    public override fun onCreate(savedInstanceState: Bundle?) {
        this.context.getApplicationInjector().inject(this)

        super.onCreate(savedInstanceState)

        this.presenter = SettingsFragmentPresenter(this, this.trackRecorderServiceController)
    }

    public override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.presenter.onViewCreated()
    }
}