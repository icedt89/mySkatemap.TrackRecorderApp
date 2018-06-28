package com.janhafner.myskatemap.apps.trackrecorder.services.calories

import android.util.Log
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

internal final class BurnedEnergyCalculator(weightInKilograms: Float,
                                            heightInCentimeters: Float,
                                            ageInYears: Int,
                                            sex: Sex,
                                            metValue: Float) : IBurnedEnergyCalculator {
    private val partiallyCompleteFormula: Float

    private val calculatedValueSubject: BehaviorSubject<BurnedEnergy> = BehaviorSubject.create<BurnedEnergy>()
    public override val calculatedValueChanged: Observable<BurnedEnergy> = this.calculatedValueSubject

    public override val calculatedValue: BurnedEnergy
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

    public override fun calculate(activityDurationInSeconds: Int) : BurnedEnergy {
        if(this.isDestroyed) {
            throw IllegalStateException("Object is destroyed!")
        }

        val kiloCalories = this.partiallyCompleteFormula * ((activityDurationInSeconds / 60.0f) / 60.0f)

        val burnedEnergy = BurnedEnergy(kiloCalories)

        this.calculatedValueSubject.onNext(burnedEnergy)

        return burnedEnergy
    }

    private var isDestroyed: Boolean = false
    public override fun destroy() {
        if(this.isDestroyed) {
            return
        }

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