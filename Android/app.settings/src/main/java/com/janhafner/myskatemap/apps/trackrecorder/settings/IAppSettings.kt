package com.janhafner.myskatemap.apps.trackrecorder.settings

import com.janhafner.myskatemap.apps.trackrecorder.common.PropertyChangedData
import io.reactivex.Observable
import java.util.*

public interface IAppSettings {
    val propertyChanged: Observable<PropertyChangedData>

    var distanceUnitFormatterTypeName: String

    var speedUnitFormatterTypeName: String

    var energyUnitFormatterTypeName: String

    var locationProviderTypeName: String

    var enableAutoPauseOnStill: Boolean

    var mapControlTypeName: String

    var appUiLocale: String

    var allowLiveTracking: Boolean

    var currentTrackRecordingId: UUID?

    var currentDashboardId: UUID?

    var vibrateOnLocationAvailabilityLoss: Boolean

    var defaultMetActivityCode: String
}