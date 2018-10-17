package com.janhafner.myskatemap.apps.trackrecorder.burnedenergy

import com.janhafner.myskatemap.apps.trackrecorder.common.types.Sex

public final class BurnedEnergyCalculator : IBurnedEnergyCalculator {
    public override fun calculateBurnedEnergy(weightInKilograms: Float,
                                              heightInCentimeters: Int,
                                              ageInYears: Int,
                                              sex: Sex,
                                              metValue: Float,
                                              activityDurationInSeconds: Int): Float {
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
        if (sex == Sex.Male) {
            basalMetabolicRate = basalMetabolicRate + basalMetabolicFactorSet.factor4
        } else {
            basalMetabolicRate = basalMetabolicRate - basalMetabolicFactorSet.factor4
        }

        val partialCompleteFormula = basalMetabolicRate / 24.0f * metValue

        val timeInHours = ((activityDurationInSeconds / 60.0f) / 60.0f)

        return partialCompleteFormula * timeInHours
    }
}


