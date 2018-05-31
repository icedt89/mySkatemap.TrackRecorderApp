package com.janhafner.myskatemap.apps.trackrecorder.services.calories

import com.janhafner.myskatemap.apps.trackrecorder.IDestroyable
import io.reactivex.Observable

internal interface IBurnedEnergyCalculator : IDestroyable {
    val calculatedValueChanged: Observable<BurnedEnergy>

    fun calculate(activityDurationInSeconds: Int)
}