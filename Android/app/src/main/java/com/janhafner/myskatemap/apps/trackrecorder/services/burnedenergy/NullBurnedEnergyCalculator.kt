package com.janhafner.myskatemap.apps.trackrecorder.services.burnedenergy

import io.reactivex.Observable

internal final class NullBurnedEnergyCalculator : IBurnedEnergyCalculator {
    public override val calculatedValueChanged: Observable<BurnedEnergy>  = Observable.never()

    public override val calculatedValue: BurnedEnergy
        get() = BurnedEnergy.empty

    public override fun calculate(activityDurationInSeconds: Int) : BurnedEnergy {
        return this.calculatedValue
    }

    public override fun destroy() {
    }
}