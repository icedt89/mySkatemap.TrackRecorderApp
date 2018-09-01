package com.janhafner.myskatemap.apps.trackrecorder.views.activities.userprofilesettings

import android.os.Bundle
import android.preference.PreferenceFragment
import android.view.View
import com.janhafner.myskatemap.apps.trackrecorder.getApplicationInjector
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.IServiceController
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderServiceBinder
import javax.inject.Inject

internal final class UserProfileSettingsFragment : PreferenceFragment() {
    @Inject
    public lateinit var trackRecorderServiceController: IServiceController<TrackRecorderServiceBinder>

    private lateinit var presenter: UserProfileSettingsFragmentPresenter

    public override fun onCreate(savedInstanceState: Bundle?) {
        this.context.getApplicationInjector().inject(this)

        super.onCreate(savedInstanceState)

        this.presenter = UserProfileSettingsFragmentPresenter(this, this.trackRecorderServiceController)
    }

    public override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.presenter.onViewCreated()
    }
}