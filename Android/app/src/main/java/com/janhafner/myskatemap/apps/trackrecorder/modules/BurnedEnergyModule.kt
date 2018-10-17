package com.janhafner.myskatemap.apps.trackrecorder.modules

import com.janhafner.myskatemap.apps.trackrecorder.burnedenergy.BurnedEnergyCalculator
import com.janhafner.myskatemap.apps.trackrecorder.burnedenergy.IBurnedEnergyCalculator
import dagger.Module
import dagger.Provides

@Module
internal final class BurnedEnergyModule {
    @Provides
    public fun provideBurnedEnergyCalculator() : IBurnedEnergyCalculator {
        return BurnedEnergyCalculator()
    }
}