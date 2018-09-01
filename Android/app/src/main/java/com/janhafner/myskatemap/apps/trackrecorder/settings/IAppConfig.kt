package com.janhafner.myskatemap.apps.trackrecorder.settings

internal interface IAppConfig {
    val stillDetectorDetectionIntervalInMilliseconds: Int

    val forceUsingOpenStreetMap: Boolean

    val trackColor: String

    val useFakeLiveLocationTrackingService: Boolean
}