package com.janhafner.myskatemap.apps.trackrecorder.services.calories

import android.util.Log
import com.janhafner.myskatemap.apps.trackrecorder.Sex
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

internal final class BurnedEnergyCalculator(weightInKilograms: Float,
                                            heightInCentimeters: Float,
                                            ageInYears: Int,
                                            sex: Sex,
                                            metValue: Float) {
    private val partiallyCompleteFormula: Float

    private val calculatedValueSubject: BehaviorSubject<BurnedEnergy> = BehaviorSubject.create<BurnedEnergy>()
    public val calculatedValueChanged: Observable<BurnedEnergy> = this.calculatedValueSubject

    public val calculatedValue: BurnedEnergy?
        get() = this.calculatedValueSubject.value

    init {
        val basalMetabolicFactorSet: BasalMetabolicFactorSet
        if (sex == Sex.Male) {
            basalMetabolicFactorSet = BasalMetabolicFactorSet.male
        } else {
            basalMetabolicFactorSet = BasalMetabolicFactorSet.female
        }

        // https://www.blitzresults.com/en/calories-burned/
        val basalMetabolicRate = (basalMetabolicFactorSet.factor1 * weightInKilograms) + (basalMetabolicFactorSet.factor2 * heightInCentimeters) - (basalMetabolicFactorSet.factor3 * ageInYears) + basalMetabolicFactorSet.factor4

        this.partiallyCompleteFormula = (basalMetabolicRate / 24.0f) * metValue
    }

    public fun calculate(activityDurationInSeconds: Int) {
        val kiloCalories = this.partiallyCompleteFormula * ((activityDurationInSeconds / 60.0f) / 60.0f)

        val burnedEnergy = BurnedEnergy(kiloCalories)

        Log.v("BurnedEnergyCalculator", burnedEnergy.toString())

        this.calculatedValueSubject.onNext(burnedEnergy)
    }

    private final class BasalMetabolicFactorSet(public val factor1: Float,
                                                              public val factor2: Float,
                                                              public val factor3: Float,
                                                              public val factor4: Float) {
        companion object {
            public val male: BasalMetabolicFactorSet = BasalMetabolicFactorSet(13.75f,
                    5.0f,
                    6.67f,
                    66.0f)

            public val female: BasalMetabolicFactorSet = BasalMetabolicFactorSet(9.56f,
                    1.85f,
                    4.68f,
                    655.0f)
        }
    }
}