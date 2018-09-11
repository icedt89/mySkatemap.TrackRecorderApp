package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.speed

import com.janhafner.myskatemap.apps.trackrecorder.conversion.speed.ISpeedConverter

internal interface ISpeedConverterFactory {
    fun createConverter(): ISpeedConverter
}