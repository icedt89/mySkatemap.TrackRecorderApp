package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.settings

import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
internal final class AppSettingsModule {

    @Provides
    @Singleton
    public fun provideAppSettings(): IAppSettings {
        return AppSettings()
    }
}