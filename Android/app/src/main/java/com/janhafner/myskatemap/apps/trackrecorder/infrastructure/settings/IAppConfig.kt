package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.settings

internal interface IAppConfig {
    val forceUsingOpenStreetMap: Boolean

    val trackColor: String

    val useFakeLiveLocationTrackingService: Boolean
}