package com.janhafner.myskatemap.apps.trackrecorder.services.burnedenergy

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject

internal final class BurnedEnergyCalculator(weightInKilograms: Float,
                                            heightInCentimeters: Int,
                                            ageInYears: Int,
                                            sex: Sex,
                                            metValue: Float) : IBurnedEnergyCalculator {
    private val partiallyCompleteFormula: Float

    private val calculatedValueSubject: BehaviorSubject<BurnedEnergy> = BehaviorSubject.create<BurnedEnergy>()
    public override val calculatedValueChanged: Observable<BurnedEnergy> = this.calculatedValueSubject.subscribeOn(Schedulers.computation())

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
        /*
            Formula MET (Metabolic Rate)
            The MET is the metabolic rate, i. e. the calorie consumption per hour per kilogram or pound of body weight.

            MET = 1\frac{kcal}{kg*h}
            However, it is also possible to calculate the kilocalorie consumption more precisely with the basal metabolic rate (BMR). This is made up of weight, size and age and a suitable formula was first used by Harris Benedict in 1919.

            \text{BMR for Women} = (9,56 * \text{Weight in kg}) + (1,85 * \text{Height in cm}) - (4,68 * \text{Age}) + 655 \text{BMR for Men} = (13,75 * \text{Weight in kg}) + (5 * \text{Height in cm}) - 6,67 * \text{Age}) + 66
            Combining the BMR (basic metabolic rate) with the MET (metabolic rate) and taking into account the time, one obtains the consumed calories for a certain activity.

            Kilocalories = \frac{BMR}{24}*MET*\text{Time in h}
         */
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