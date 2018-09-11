package com.janhafner.myskatemap.apps.trackrecorder.services.burnedenergy

import com.janhafner.myskatemap.apps.trackrecorder.common.IDestroyable
import io.reactivex.Observable

internal interface IBurnedEnergyCalculator : IDestroyable {
    val calculatedValueChanged: Observable<BurnedEnergy>

    val calculatedValue: BurnedEnergy?

    fun calculate(activityDurationInSeconds: Int) : BurnedEnergy
}