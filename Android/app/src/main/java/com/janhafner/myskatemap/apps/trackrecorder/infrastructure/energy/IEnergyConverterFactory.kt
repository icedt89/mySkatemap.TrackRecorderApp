package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.energy

import com.janhafner.myskatemap.apps.trackrecorder.conversion.energy.IEnergyConverter

internal interface IEnergyConverterFactory {
    fun createConverter() : IEnergyConverter
}