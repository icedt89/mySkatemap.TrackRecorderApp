package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.settings

internal interface IAppSettings: IAppSettingsChanged {
    var trackDistanceUnitFormatterTypeName: String

    var vibrateOnBackgroundStop: Boolean

    var locationProviderTypeName: String

    var notificationFlashColorOnBackgroundStop: Int

    var appUiLocale: String

    var allowLiveTracking: Boolean
}