package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.settings

import java.util.*

internal interface IAppSettings: IAppSettingsChanged {
    var trackDistanceUnitFormatterTypeName: String

    var trackColor: Int

    var vibrateOnBackgroundStop: Boolean

    var locationProviderTypeName: String

    var notificationFlashColorOnBackgroundStop: Int

    var mapStyleResourceName: String

    var appUiLocale: String
}