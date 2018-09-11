package com.janhafner.myskatemap.apps.trackrecorder.modules

import com.janhafner.myskatemap.apps.trackrecorder.formatting.speed.SpeedConverterFactory
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance.DistanceConverterFactory
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance.IDistanceConverterFactory
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.energy.EnergyConverterFactory
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.energy.IEnergyConverterFactory
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.speed.ISpeedConverterFactory
import com.janhafner.myskatemap.apps.trackrecorder.settings.IAppSettings
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
internal final class ConversionModule {
    @Provides
    @Singleton
    public fun provideDistanceConverterFactory(appSettings: IAppSettings): IDistanceConverterFactory {
        return DistanceConverterFactory(appSettings)
    }

    @Provides
    @Singleton
    public fun provideSpeedConverterFactory(appSettings: IAppSettings): ISpeedConverterFactory {
        return SpeedConverterFactory(appSettings)
    }

    @Provides
    @Singleton
    public fun provideEnergyConverterFactory(appSettings: IAppSettings): IEnergyConverterFactory {
        return EnergyConverterFactory(appSettings)
    }
}