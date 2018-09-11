package com.janhafner.myskatemap.apps.trackrecorder.settings

import com.janhafner.myskatemap.apps.trackrecorder.common.PropertyChangedData
import com.janhafner.myskatemap.apps.trackrecorder.common.Sex
import io.reactivex.Observable

public interface IUserProfileSettings {
    val propertyChanged: Observable<PropertyChangedData>

    var enableCalculationOfBurnedEnergy: Boolean

    var name: String?

    var age: Int?

    var height: Int?

    var weight: Float?

    var sex: Sex?
}

