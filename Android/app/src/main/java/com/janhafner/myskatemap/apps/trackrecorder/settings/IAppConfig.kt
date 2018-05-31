package com.janhafner.myskatemap.apps.trackrecorder.settings

internal interface IAppConfig {
    val forceUsingOpenStreetMap: Boolean

    val trackColor: String

    val useFakeLiveLocationTrackingService: Boolean

    val updateBurnedEnergySeconds: Int

    val updateStatisticsSeconds: Int

    val updateTrackRecordingLocationsSeconds: Int

    val updateTrackDistanceSeconds: Int

    val updateLiveLocationSession: Int
}