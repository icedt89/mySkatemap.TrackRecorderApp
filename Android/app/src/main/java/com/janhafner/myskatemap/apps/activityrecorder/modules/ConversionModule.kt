package com.janhafner.myskatemap.apps.activityrecorder.modules

import com.janhafner.myskatemap.apps.activityrecorder.formatting.speed.SpeedConverterFactory
import com.janhafner.myskatemap.apps.activityrecorder.infrastructure.distance.DistanceConverterFactory
import com.janhafner.myskatemap.apps.activityrecorder.conversion.distance.IDistanceConverterFactory
import com.janhafner.myskatemap.apps.activityrecorder.infrastructure.energy.EnergyConverterFactory
import com.janhafner.myskatemap.apps.activityrecorder.conversion.energy.IEnergyConverterFactory
import com.janhafner.myskatemap.apps.activityrecorder.conversion.speed.ISpeedConverterFactory
import com.janhafner.myskatemap.apps.activityrecorder.settings.IAppSettings
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