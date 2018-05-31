package com.janhafner.myskatemap.apps.trackrecorder.statistics

import com.janhafner.myskatemap.apps.trackrecorder.IDestroyable
import com.janhafner.myskatemap.apps.trackrecorder.io.data.Location

internal interface ITrackRecordingStatistic : IDestroyable {
    val speed: Statistic

    val altitude: Statistic

    fun addAll(location: List<Location>)

    fun add(location: Location)
}