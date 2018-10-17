package com.janhafner.myskatemap.apps.trackrecorder.modules

import com.janhafner.myskatemap.apps.trackrecorder.distancecalculation.DistanceCalculator
import com.janhafner.myskatemap.apps.trackrecorder.distancecalculation.IDistanceCalculator
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
internal final class DistanceCalculationModule {
    @Provides
    @Singleton
    public fun provideDistanceCalculator(): IDistanceCalculator {
        return DistanceCalculator()
    }
}