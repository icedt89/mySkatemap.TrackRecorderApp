package com.janhafner.myskatemap.apps.trackrecorder.settings

import com.janhafner.myskatemap.apps.trackrecorder.common.PropertyChangedData
import io.reactivex.Observable

public interface IAppSettings {
    val propertyChanged: Observable<PropertyChangedData>

    var distanceConverterTypeName: String

    var speedConverterTypeName: String

    var energyConverterTypeName: String

    var enableAutoPauseOnStill: Boolean

    var mapControlTypeName: String

    var appUiLocale: String

    var vibrateOnLocationAvailabilityLoss: Boolean

    var defaultMetActivityCode: String
}