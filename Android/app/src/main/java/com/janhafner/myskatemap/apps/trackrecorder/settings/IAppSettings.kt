package com.janhafner.myskatemap.apps.trackrecorder.settings

import io.reactivex.Observable
import java.util.*

internal interface IAppSettings {
    val propertyChanged: Observable<PropertyChangedData>

    var distanceUnitFormatterTypeName: String

    var speedUnitFormatterTypeName: String

    var burnedEnergyUnitFormatterTypeName: String

    var locationProviderTypeName: String

    var appUiLocale: String

    var allowLiveTracking: Boolean

    var currentTrackRecordingId: UUID?

    var currentDashboardId: UUID?

    var defaultMetActivityCode: String
}