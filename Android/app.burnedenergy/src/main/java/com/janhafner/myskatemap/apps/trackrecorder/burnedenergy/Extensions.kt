package com.janhafner.myskatemap.apps.trackrecorder.burnedenergy

import com.janhafner.myskatemap.apps.trackrecorder.common.types.Sex
import io.reactivex.Observable
import io.reactivex.ObservableTransformer

public fun IBurnedEnergyCalculator.toObservableTransformer(weightInKilograms: Float,
                                                           heightInCentimeters: Int,
                                                           ageInYears: Int,
                                                           sex: Sex,
                                                           metValue: Float) : ObservableTransformer<Int, Float> {
    return BurnedEnergyCalculatorTransformer(this, weightInKilograms, heightInCentimeters, ageInYears, sex, metValue)
}

public fun Observable<Int>.calculateBurnedEnergy(burnedEnergyCalculator: IBurnedEnergyCalculator, weightInKilograms: Float,
                                                 heightInCentimeters: Int,
                                                 ageInYears: Int,
                                                 sex: Sex,
                                                 metValue: Float): Observable<Float> {
    return this.compose(burnedEnergyCalculator.toObservableTransformer(weightInKilograms,
            heightInCentimeters,
            ageInYears,
            sex,
            metValue))
}