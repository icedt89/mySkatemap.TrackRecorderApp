package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.calories

import android.util.Log
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

internal final class BurnedEnergyCalculator(weightInKilograms: Float,
                                            heightInCentimeters: Int,
                                            ageInYears: Int,
                                            sex: Sex,
                                            activityMetabolicEquivalent: Float) {
    private val partiallyCompleteFormula: Float

    private val calculatedValueSubject: BehaviorSubject<BurnedEnergy> = BehaviorSubject.create<BurnedEnergy>()
    public val calculatedValueChanged: Observable<BurnedEnergy> = this.calculatedValueSubject

    public val calculatedValue: BurnedEnergy?
        get() = this.calculatedValueSubject.value

    init {
        val basalMetabolicFactorset: BasalMetabolicFactorSet
        if (sex == Sex.Male) {
            basalMetabolicFactorset = BasalMetabolicFactorSet.male
        } else {
            basalMetabolicFactorset = BasalMetabolicFactorSet.female
        }

        // https://www.blitzresults.com/en/calories-burned/
        val basalMetabolicRate = (basalMetabolicFactorset.factor1 * weightInKilograms) + (basalMetabolicFactorset.factor2 * heightInCentimeters) - (basalMetabolicFactorset.factor3 * ageInYears) + basalMetabolicFactorset.factor4

        this.partiallyCompleteFormula = (basalMetabolicRate / 24.0f) * activityMetabolicEquivalent
    }

    public fun calculate(activityDurationInSeconds: Int) {
        if (this.isDestroyed) {
            throw IllegalStateException("Instance is already destroyed!")
        }

        val kiloCalories = this.partiallyCompleteFormula * ((activityDurationInSeconds / 60.0f) / 60.0f)

        val burnedEnergy = BurnedEnergy(kiloCalories)

        Log.v("BurnedEnergyCalculator", burnedEnergy.toString())

        this.calculatedValueSubject.onNext(burnedEnergy)
    }

    private var isDestroyed: Boolean = false
    public fun destroy() {
        this.calculatedValueSubject.onComplete()

        this.isDestroyed = true
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