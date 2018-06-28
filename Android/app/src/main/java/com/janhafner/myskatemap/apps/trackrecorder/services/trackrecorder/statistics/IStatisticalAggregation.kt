package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.statistics

import com.janhafner.myskatemap.apps.trackrecorder.IDestroyable
import com.janhafner.myskatemap.apps.trackrecorder.io.data.Location
import com.janhafner.myskatemap.apps.trackrecorder.services.calories.BurnedEnergy
import com.janhafner.myskatemap.apps.trackrecorder.statistics.IStatistic
import io.reactivex.Observable
import org.joda.time.Period

internal interface IStatisticalAggregation : IDestroyable {
    val burnedEnergyChanged: Observable<BurnedEnergy>

    val burnedEnergy: BurnedEnergy

    val recordingTimeChanged: Observable<Period>

    val recordingTime: Period

    val trackDistanceChanged: Observable<Float>

    val trackDistance: Float

    val speed: IStatistic

    val altitude: IStatistic

     fun addAll(location: List<Location>)

     fun add(location: Location)
}