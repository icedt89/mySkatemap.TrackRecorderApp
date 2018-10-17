package com.janhafner.myskatemap.apps.trackrecorder.conversion.distance

public interface IDistanceConverterFactory {
    fun createConverter(): IDistanceConverter
}