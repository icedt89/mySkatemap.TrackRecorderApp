package com.janhafner.myskatemap.apps.trackrecorder.services.burnedenergy

import com.janhafner.myskatemap.apps.trackrecorder.common.Sex
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject

internal final class BurnedEnergyCalculator(weightInKilograms: Float,
                                            heightInCentimeters: Int,
                                            ageInYears: Int,
                                            sex: Sex,
                                            private val metValue: Float) : IBurnedEnergyCalculator {
    private val partialCompleteFormula: Float

    private val calculatedValueSubject: BehaviorSubject<BurnedEnergy> = BehaviorSubject.create<BurnedEnergy>()
    public override val calculatedValueChanged: Observable<BurnedEnergy> = this.calculatedValueSubject.subscribeOn(Schedulers.computation())

    public override val calculatedValue: BurnedEnergy?
        get() = this.calculatedValueSubject.value

    init {
        val basalMetabolicFactorSet: BasalMetabolicFactorSet
        if (sex == Sex.Male) {
            basalMetabolicFactorSet = BasalMetabolicFactorSet.male
        } else {
            basalMetabolicFactorSet = BasalMetabolicFactorSet.female
        }

        // https://www.blitzresults.com/en/calories-burned/
        // https://en.wikipedia.org/wiki/Harris%E2%80%93Benedict_equation
        var basalMetabolicRate = (basalMetabolicFactorSet.factor1 * weightInKilograms)
            + (basalMetabolicFactorSet.factor2 * heightInCentimeters)
            - (basalMetabolicFactorSet.factor3 * ageInYears)
        if(sex == Sex.Male) {
            basalMetabolicRate = basalMetabolicRate + basalMetabolicFactorSet.factor4
        } else {
            basalMetabolicRate = basalMetabolicRate - basalMetabolicFactorSet.factor4
        }

        this.partialCompleteFormula = basalMetabolicRate / 24.0f * metValue
    }

    public override fun calculate(activityDurationInSeconds: Int) : BurnedEnergy {
        if(this.isDestroyed) {
            throw IllegalStateException("Object is destroyed!")
        }

        val timeInHours = ((activityDurationInSeconds / 60.0f) / 60.0f)
        val kiloCalories = this.partialCompleteFormula * timeInHours

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
            public val male: BasalMetabolicFactorSet = BasalMetabolicFactorSet(10.0f,
                    6.25f,
                    5.0f,
                    5.0f)

            public val female: BasalMetabolicFactorSet = BasalMetabolicFactorSet(10.0f,
                    6.25f,
                    5.0f,
                    161.0f)
        }
    }
}