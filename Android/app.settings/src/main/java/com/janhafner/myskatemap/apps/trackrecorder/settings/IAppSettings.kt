package com.janhafner.myskatemap.apps.trackrecorder.settings

import com.janhafner.myskatemap.apps.trackrecorder.core.IDestroyable
import com.janhafner.myskatemap.apps.trackrecorder.core.PropertyChangedData
import io.reactivex.Observable

public interface IAppSettings : IDestroyable {
    val propertyChanged: Observable<PropertyChangedData>

    var distanceConverterTypeName: String

    var speedConverterTypeName: String

    var energyConverterTypeName: String

    var enableAutoPauseOnStill: Boolean

    var enableLiveLocation: Boolean

    var mapControlTypeName: String

    var showMyLocation: Boolean

    var appUiLocale: String

    var vibrateOnLocationAvailabilityLoss: Boolean

    var defaultMetActivityCode: String

    var keepScreenOn: Boolean

    var showPositionsOnMap: Boolean
}