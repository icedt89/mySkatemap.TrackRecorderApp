package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder

import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance.TrackDistanceModule
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.settings.AppSettingsModule
import dagger.Component


@Component(modules = [TrackDistanceModule::class, AppSettingsModule::class])
internal interface TrackRecorderServiceComponent {
    fun inject(trackRecorderService: ITrackRecorderService)
}