package com.janhafner.myskatemap.apps.trackrecorder.statistics

import com.janhafner.myskatemap.apps.trackrecorder.IDestroyable
import com.janhafner.myskatemap.apps.trackrecorder.io.data.Location
import com.janhafner.myskatemap.apps.trackrecorder.services.temperature.Temperature

internal interface ITrackRecordingStatistic : IDestroyable {
    val speed: Statistic

    val altitude: Statistic

    val ambientTemperature: Statistic

    fun addAll(location: List<Location>)

    fun add(location: Location)

    fun addAmbientTemperature(temperature: Temperature)

    fun addAllAmbientTemperatures(temperatures: List<Temperature>)
}