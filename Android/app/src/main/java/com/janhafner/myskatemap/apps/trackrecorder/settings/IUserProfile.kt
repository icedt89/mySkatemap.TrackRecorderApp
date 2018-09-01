package com.janhafner.myskatemap.apps.trackrecorder.settings

import com.janhafner.myskatemap.apps.trackrecorder.services.calories.Sex
import io.reactivex.Observable

internal interface IUserProfile {
    val propertyChanged: Observable<PropertyChangedData>

    var enableCalculationOfBurnedEnergy: Boolean

    var name: String?

    var age: Int?

    var height: Int?

    var weight: Float?

    var sex: Sex?
}

