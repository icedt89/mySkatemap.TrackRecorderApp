package com.janhafner.myskatemap.apps.trackrecorder.burnedenergy

import com.janhafner.myskatemap.apps.trackrecorder.common.types.Sex

public interface IBurnedEnergyCalculator {
    fun calculateBurnedEnergy(weightInKilograms: Float,
                              heightInCentimeters: Int,
                              ageInYears: Int,
                              sex: Sex,
                              metValue: Float,
                              activityDurationInSeconds: Int) : Float
}