package com.janhafner.myskatemap.apps.trackrecorder.services.models

import com.janhafner.myskatemap.apps.trackrecorder.common.Sex

public final class UserProfile(public val age: Int,
                                 public val metActivityCode: String,
                                 public val weightInKilograms: Float,
                                 public val heightInCentimeters: Int,
                                 public val sex: Sex) {
}
