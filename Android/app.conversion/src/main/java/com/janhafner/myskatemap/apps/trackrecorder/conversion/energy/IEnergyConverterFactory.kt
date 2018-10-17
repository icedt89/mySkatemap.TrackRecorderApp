package com.janhafner.myskatemap.apps.trackrecorder.conversion.energy

public interface IEnergyConverterFactory {
    fun createConverter() : IEnergyConverter
}