package com.janhafner.myskatemap.apps.trackrecorder.infrastructure

import com.janhafner.myskatemap.apps.trackrecorder.common.ObjectDestroyedException
import com.janhafner.myskatemap.apps.trackrecorder.common.aggregations.Aggregation
import com.janhafner.myskatemap.apps.trackrecorder.common.types.Location


internal final class LocationsAggregation : ILocationsAggregation {
    public override val speed = Aggregation()

    public override val altitude = Aggregation()

    public override fun addAll(location: List<Location>) {
        if(this.isDestroyed) {
            throw ObjectDestroyedException()
        }

        this.speed.addAll(location.map {
            if(it.speed == null) {
                0.0f
            } else {
                it.speed!!
            }
        })

        this.altitude.addAll(location.map {
            if(it.altitude == null) {
                0.0f
            } else {
                it.altitude!!.toFloat()
            }
        })
    }

    public override fun add(location: Location) {
        if(this.isDestroyed) {
            throw ObjectDestroyedException()
        }

        if(location.speed == null) {
            this.speed.add(0.0f)
        } else {
            this.speed.add(location.speed!!.toFloat())
        }

        if(location.altitude == null) {
            this.altitude.add(0.0f)
        } else {
            this.altitude.add(location.altitude!!.toFloat())
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