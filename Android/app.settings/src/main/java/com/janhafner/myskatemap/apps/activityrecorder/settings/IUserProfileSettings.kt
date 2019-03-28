package com.janhafner.myskatemap.apps.activityrecorder.settings

import com.janhafner.myskatemap.apps.activityrecorder.core.PropertyChangedData
import com.janhafner.myskatemap.apps.activityrecorder.core.types.Sex
import io.reactivex.Observable

public interface IUserProfileSettings {
    val propertyChanged: Observable<PropertyChangedData>

    var name: String?

    var age: Int?

    var height: Int?

    var weight: Float?

    var sex: Sex?
}

