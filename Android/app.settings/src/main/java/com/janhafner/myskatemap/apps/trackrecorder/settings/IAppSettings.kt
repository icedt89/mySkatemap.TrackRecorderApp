package com.janhafner.myskatemap.apps.trackrecorder.settings

import com.janhafner.myskatemap.apps.trackrecorder.common.IDestroyable
import com.janhafner.myskatemap.apps.trackrecorder.common.PropertyChangedData
import io.reactivex.Observable

public interface IAppSettings : IDestroyable {
    val propertyChanged: Observable<PropertyChangedData>

    var distanceConverterTypeName: String

    var speedConverterTypeName: String

    var energyConverterTypeName: String

    var enableAutoPauseOnStill: Boolean

    var enableLiveLocation: Boolean

    var mapControlTypeName: String

    var enableShowMyLocation: Boolean

    var appUiLocale: String

    var vibrateOnLocationAvailabilityLoss: Boolean

    var defaultMetActivityCode: String
}