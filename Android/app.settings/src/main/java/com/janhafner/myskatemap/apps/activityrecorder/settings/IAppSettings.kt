package com.janhafner.myskatemap.apps.activityrecorder.settings

import com.janhafner.myskatemap.apps.activityrecorder.core.IDestroyable
import com.janhafner.myskatemap.apps.activityrecorder.core.PropertyChangedData
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

    var vibrateOnLocationAvailabilityLoss: Boolean

    var keepScreenOn: Boolean

    var showPositionsOnMap: Boolean
}