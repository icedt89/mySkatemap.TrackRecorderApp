package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider

internal interface IMyLocationProvider {
    fun getMyCurrentLocation() : IMyLocationRequestState
}

