package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.settings

import java.util.*

internal interface IAppSettings: IAppSettingsChanged {
    var trackDistanceUnitFormatterTypeName: String

    var vibrateOnBackgroundStop: Boolean

    var locationProviderTypeName: String

    var notificationFlashColorOnBackgroundStop: Int

    var appUiLocale: String

    var allowLiveTracking: Boolean

    var currentTrackRecordingId: UUID?
}