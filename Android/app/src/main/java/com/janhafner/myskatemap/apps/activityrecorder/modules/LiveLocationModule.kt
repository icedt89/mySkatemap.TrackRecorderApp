package com.janhafner.myskatemap.apps.activityrecorder.modules

import android.util.Log
import com.janhafner.myskatemap.apps.activityrecorder.BuildConfig
import com.janhafner.myskatemap.apps.activityrecorder.live.*
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.ILiveSessionController
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.LiveSessionController
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
internal final class LiveLocationModule {
    @Provides
    @Singleton
    public fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().build()
    }

    @Singleton
    @Provides
    public fun provideHttpLiveSessionApiClient(httpClient: OkHttpClient): HttpLiveSessionApiClient {
        val baseUrl = BuildConfig.LIVE_LOCATION_HTTP_BASE_URL

        Log.v("LiveLocationModule", "Using \"${baseUrl}\" as base url for requests")

        return HttpLiveSessionApiClient(httpClient, baseUrl)
    }

    @Provides
    @Singleton
    public fun provideLiveSessionProvider(httpLiveSessionApiClient: HttpLiveSessionApiClient, moshi: Moshi) : ILiveSessionProvider {
        if (BuildConfig.LIVE_LOCATION_USE_HTTP) {
            Log.v("LiveLocationModule", "Using HttpLiveSessionProvider implementation")

            return HttpLiveSessionProvider(httpLiveSessionApiClient, moshi)
        }

        Log.v("LiveLocationModule", "Using NullLiveSessionProvider implementation")

        return NullLiveSessionProvider()
    }

    @Provides
    public fun provideLiveSessionController(liveSessionProvider: ILiveSessionProvider): ILiveSessionController {
        return LiveSessionController(liveSessionProvider)
    }

    @Singleton
    @Provides
    public fun provideMoshi(): Moshi {
        return Moshi.Builder()
                .add(JodaTimeDateTimeMoshiAdapter())
                .add(JodaTimePeriodMoshiAdapter())
                // .add(KotlinJsonAdapterFactory())
                .build()
    }
}