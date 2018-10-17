package com.janhafner.myskatemap.apps.trackrecorder.burnedenergy

import com.janhafner.myskatemap.apps.trackrecorder.common.types.Sex
import io.reactivex.Observable
import io.reactivex.ObservableSource

public final class BurnedEnergyCalculatorTransformer(private val burnedEnergyCalculator: IBurnedEnergyCalculator,
                                                     private val weightInKilograms: Float,
                                                     private val heightInCentimeters: Int,
                                                     private val ageInYears: Int,
                                                     private val sex: Sex,
                                                     private val metValue: Float) : io.reactivex.ObservableTransformer<Int, Float> {
    public override fun apply(upstream: Observable<Int>) : ObservableSource<Float> {
        return upstream
                .map {
                    this.burnedEnergyCalculator.calculateBurnedEnergy(this.weightInKilograms,
                            this.heightInCentimeters,
                            this.ageInYears,
                            this.sex,
                            this.metValue,
                            it)
                }
    }
}