package com.janhafner.myskatemap.apps.trackrecorder.infrastructure

import com.janhafner.myskatemap.apps.trackrecorder.common.ObjectDestroyedException
import com.janhafner.myskatemap.apps.trackrecorder.common.aggregations.NewAggregation
import com.janhafner.myskatemap.apps.trackrecorder.common.types.Location
import io.reactivex.Observable

internal final class NewLocationsAggregation(values: Observable<Location>) : INewLocationsAggregation {
    public override val speed = NewAggregation(values.map {
        if(it.speed == null) {
            0.0
        } else {
            it.speed!!.toDouble()
        }
    })

    public override val altitude = NewAggregation(values.map {
        if(it.altitude == null) {
            0.0
        } else {
            it.altitude!!
        }
    })

    public override fun addAll(location: List<Location>) {
        if(this.isDestroyed) {
            throw ObjectDestroyedException()
        }

        this.speed.addAll(location.map {
            if(it.speed == null) {
                0.0
            } else {
                it.speed!!.toDouble()
            }
        })

        this.altitude.addAll(location.map {
            if(it.altitude == null) {
                0.0
            } else {
                it.altitude!!
            }
        })
    }

    public override fun add(location: Location) {
        if(this.isDestroyed) {
            throw ObjectDestroyedException()
        }

        if(location.speed == null) {
            this.speed.add(0.0)
        } else {
            this.speed.add(location.speed!!.toDouble())
        }

        if(location.altitude == null) {
            this.altitude.add(0.0)
        } else {
            this.altitude.add(location.altitude!!)
        }
    }

    private var isDestroyed: Boolean = false
    public override fun destroy() {
        if(this.isDestroyed) {
            return
        }

        this.altitude.destroy()
        this.speed.destroy()

        this.isDestroyed = true
    }
}