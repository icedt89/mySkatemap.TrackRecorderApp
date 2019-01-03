package com.janhafner.myskatemap.apps.trackrecorder.infrastructure

import com.janhafner.myskatemap.apps.trackrecorder.common.aggregations.Aggregation
import com.janhafner.myskatemap.apps.trackrecorder.common.types.Location
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable

internal final class LocationsAggregation(values: Observable<Location>) : ILocationsAggregation {
    private val subscriptions: CompositeDisposable = CompositeDisposable()

    public override val speed = Aggregation(values.map {
        if(it.speed == null) {
            0.0
        } else {
            it.speed!!.toDouble()
        }
    })

    public override val altitude = Aggregation(values.map {
        if(it.altitude == null) {
            0.0
        } else {
            it.altitude!!
        }
    })

    init {
        this.altitude.minimumValueChanged.subscribe()
        this.altitude.maximumValueChanged.subscribe()
        this.altitude.averageValueChanged.subscribe()
        this.altitude.firstValueChanged.subscribe()
        this.altitude.latestValueChanged.subscribe()

        this.speed.minimumValueChanged.subscribe()
        this.speed.maximumValueChanged.subscribe()
        this.speed.averageValueChanged.subscribe()
        this.speed.firstValueChanged.subscribe()
        this.speed.latestValueChanged.subscribe()
    }

    private var isDestroyed = false
    public override fun destroy() {
        if(isDestroyed) {
            return
        }

        this.subscriptions.dispose()

        this.isDestroyed = true
    }
}