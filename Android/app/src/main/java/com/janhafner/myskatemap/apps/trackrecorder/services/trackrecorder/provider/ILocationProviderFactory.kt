package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider

internal interface ILocationProviderFactory {
    fun createLocationProvider(locationProviderTypeName: String? = null) : ILocationProvider
}