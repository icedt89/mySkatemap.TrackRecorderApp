package com.janhafner.myskatemap.apps.trackrecorder.modules

import com.janhafner.myskatemap.apps.trackrecorder.BuildConfig
import com.janhafner.myskatemap.apps.trackrecorder.live.*
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
        return OkHttpClient()
    }

    @Singleton
    @Provides
    public fun provideHttpLiveSessionApiClient(httpClient: OkHttpClient): HttpLiveSessionApiClient {
        val baseUrl = BuildConfig.LIVE_LOCATION_HTTP_BASE_URL

        return HttpLiveSessionApiClient(httpClient, baseUrl)
    }

    @Provides
    @Singleton
    public fun provideLiveSessionProvider(httpLiveSessionApiClient: HttpLiveSessionApiClient, moshi: Moshi) : ILiveSessionProvider {
        if (BuildConfig.LIVE_LOCATION_USE_HTTP) {
            return HttpLiveSessionProvider(httpLiveSessionApiClient, moshi)
        }

        return NullLiveSessionProvider()
    }

    @Singleton
    @Provides
    public fun provideMoshi(): Moshi {
        return Moshi.Builder()
                .add(JodaTimeDateTimeMoshiAdapter())
                .add(JodaTimePeriodMoshiAdapter())
                .add(UuidMoshiAdapter())
                // .add(KotlinJsonAdapterFactory())
                .build()
    }
}