package com.janhafner.myskatemap.apps.trackrecorder.services.live

import com.janhafner.myskatemap.apps.trackrecorder.BuildConfig
import com.janhafner.myskatemap.apps.trackrecorder.JsonRestApiClient
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings

internal interface ILiveLocationTrackingServiceFactory {
    fun createService() : ILiveLocationTrackingService
}

internal final class LiveLocationTrackingServiceFactory(private val appSettings: IAppSettings,
                                                        private val jsonRestApiClient: JsonRestApiClient) : ILiveLocationTrackingServiceFactory {
    public override fun createService(): ILiveLocationTrackingService {
        if (!BuildConfig.TRACKING_ENABLE_LIVELOCATIONTRACKING) {
            return NullLiveLocationTrackingService()
        } else if(!this.appSettings.allowLiveTracking && BuildConfig.TRACKING_USE_FAKELIVELOCATIONTRACKINGSERVICE) {
            return FakeLiveLocationTrackingService()
        }

        return LiveLocationTrackingService(jsonRestApiClient)
    }
}