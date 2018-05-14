package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.calories

import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

internal final class BurnedEnergyCalculator(weightInKilograms: Float,
                                            heightInCentimeters: Int,
                                            ageInYears: Int,
                                            sex: Sex,
                                            activityMetabolicEquivalent: Float) {
    private val partiallyCompleteFormula: Double

    private val calculatedValueSubject: BehaviorSubject<BurnedEnergy> = BehaviorSubject.create<BurnedEnergy>()
    public val calculatedValueChanged: Observable<BurnedEnergy> = this.calculatedValueSubject

    public val calculatedValue: BurnedEnergy?
        get() = this.calculatedValueSubject.value

    init {
        var factor1 = 9.56
        var factor2 = 1.85
        var factor3 =  4.68
        var factor4 = 655
        if(sex == Sex.Male) {
            factor1 = 13.75
            factor2 = 5.0
            factor3 = 6.67
            factor4 = 66
        }

        // https://www.blitzresults.com/en/calories-burned/
        val basalMetabolicRate = (factor1 * weightInKilograms) + (factor2 * heightInCentimeters) - (factor3 * ageInYears) + factor4

        this.partiallyCompleteFormula = (basalMetabolicRate / 24) * activityMetabolicEquivalent
    }


    public fun calculate(activityDurationInMinutes: Int) {
        val kiloCalories = this.partiallyCompleteFormula * (activityDurationInMinutes / 60)

        val burnedEnergy = BurnedEnergy(kiloCalories)

        this.calculatedValueSubject.onNext(burnedEnergy)
    }
}