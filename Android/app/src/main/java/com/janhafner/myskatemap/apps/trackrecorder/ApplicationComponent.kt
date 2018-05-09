package com.janhafner.myskatemap.apps.trackrecorder

import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.TrackRecorderService
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.settings.SettingsFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.start.StartActivity
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.TrackRecorderActivity
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.attachments.AttachmentsTabFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.data.DataTabFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.map.MapTabFragment
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ApplicationModule::class])
internal interface ApplicationComponent {
    fun inject(trackRecorderService: TrackRecorderService)

    fun inject(startActivity: StartActivity)

    fun inject(trackRecorderActivity: TrackRecorderActivity)

    fun inject(mapTabFragment: MapTabFragment)

    fun inject(dataTabFragment: DataTabFragment)

    fun inject(attachmentsTabFragment: AttachmentsTabFragment)

   fun inject(settingsFragment: SettingsFragment)
}