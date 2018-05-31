package com.janhafner.myskatemap.apps.trackrecorder.settings

import com.janhafner.myskatemap.apps.trackrecorder.services.calories.Sex
import java.util.*

internal interface IAppSettings: IAppSettingsChanged {
    var trackDistanceUnitFormatterTypeName: String

    var locationProviderTypeName: String

    var appUiLocale: String

    var allowLiveTracking: Boolean

    var currentTrackRecordingId: UUID?

    var enableFitnessActivityTracking: Boolean

    var userAge: Int

    var defaultMetActivityCode: String

    var userWeightInKilograms: Float

    var userHeightInCentimeters: Float

    var userSex: Sex
}