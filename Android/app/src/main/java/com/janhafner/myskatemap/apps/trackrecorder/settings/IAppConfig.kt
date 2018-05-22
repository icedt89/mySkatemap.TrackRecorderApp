package com.janhafner.myskatemap.apps.trackrecorder.settings

internal interface IAppConfig {
    val forceUsingOpenStreetMap: Boolean

    val trackColor: String

    val useFakeLiveLocationTrackingService: Boolean
}