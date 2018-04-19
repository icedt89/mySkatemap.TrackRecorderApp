package com.janhafner.myskatemap.apps.trackrecorder.views.map

internal interface ITrackRecorderMapWithDelayedInitialization : ITrackRecorderMap {
    fun getMapAsync(callback: OnTrackRecorderMapReadyCallback)
}