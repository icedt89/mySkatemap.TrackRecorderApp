package com.janhafner.myskatemap.apps.trackrecorder.services.live

import com.janhafner.myskatemap.apps.trackrecorder.JsonRestApiClient
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppConfig
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings

internal interface ILiveLocationTrackingServiceFactory {
    fun createService() : ILiveLocationTrackingService
}

internal final class LiveLocationTrackingServiceFactory(private val appConfig: IAppConfig,
                                                        private val appSettings: IAppSettings,
                                                        private val jsonRestApiClient: JsonRestApiClient) : ILiveLocationTrackingServiceFactory {
    public override fun createService(): ILiveLocationTrackingService {
        if(this.appConfig.useFakeLiveLocationTrackingService) {
            return FakeLiveLocationTrackingService()
        }

        if(this.appSettings.allowLiveTracking) {
            return LiveLocationTrackingService(this.jsonRestApiClient)
        }

        return NullLiveLocationTrackingService()
    }
}