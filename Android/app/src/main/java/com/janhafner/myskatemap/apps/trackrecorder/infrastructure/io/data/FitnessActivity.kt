package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.data

import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.calories.Sex

internal final class FitnessActivity(public val age: Int,
                                     public val metabolicEquivalentActivityCode: String,
                                     public val weightInKilograms: Float,
                                     public val heightInCentimeters: Float,
                                     public val sex: Sex) {
}