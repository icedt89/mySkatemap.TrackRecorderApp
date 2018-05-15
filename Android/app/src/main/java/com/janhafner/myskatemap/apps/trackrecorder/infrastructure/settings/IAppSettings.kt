package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.settings

import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.calories.Sex
import java.util.*

internal interface IAppSettings: IAppSettingsChanged {
    var trackDistanceUnitFormatterTypeName: String

    var vibrateOnBackgroundStop: Boolean

    var locationProviderTypeName: String

    var appUiLocale: String

    var allowLiveTracking: Boolean

    var currentTrackRecordingId: UUID?

    var enableFitnessActivityTracking: Boolean

    var userAge: Int

    var userWeightInKilograms: Float

    var userHeightInCentimeters: Float

    var userSex: Sex
}