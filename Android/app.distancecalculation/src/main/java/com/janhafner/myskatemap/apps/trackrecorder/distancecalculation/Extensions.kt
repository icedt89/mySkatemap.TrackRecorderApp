package com.janhafner.myskatemap.apps.trackrecorder.distancecalculation

import com.janhafner.myskatemap.apps.trackrecorder.common.types.Location
import io.reactivex.ObservableTransformer

public fun IDistanceCalculator.toObservableTransformer() : ObservableTransformer<List<Location>, Float> {
    return DistanceCalculatorTransformer(this)
}