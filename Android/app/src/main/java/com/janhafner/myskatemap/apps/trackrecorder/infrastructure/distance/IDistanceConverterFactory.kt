package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.distance

import com.janhafner.myskatemap.apps.trackrecorder.conversion.distance.IDistanceConverter

internal interface IDistanceConverterFactory {
    fun createConverter(): IDistanceConverter
}