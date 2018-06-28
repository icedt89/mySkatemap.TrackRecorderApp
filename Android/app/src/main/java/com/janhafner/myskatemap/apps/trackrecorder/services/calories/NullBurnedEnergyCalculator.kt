package com.janhafner.myskatemap.apps.trackrecorder.services.calories

import io.reactivex.Observable

internal final class NullBurnedEnergyCalculator : IBurnedEnergyCalculator {
    private val calculatedValueChangedSubject: Observable<BurnedEnergy> = Observable.never<BurnedEnergy>()
    public override val calculatedValueChanged: Observable<BurnedEnergy>
        get() = this.calculatedValueChangedSubject

    public override val calculatedValue: BurnedEnergy
        get() = BurnedEnergy.empty

    public override fun calculate(activityDurationInSeconds: Int) : BurnedEnergy {
        return this.calculatedValue
    }

    public override fun destroy() {
    }
}