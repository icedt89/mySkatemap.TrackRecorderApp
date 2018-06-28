package com.janhafner.myskatemap.apps.trackrecorder.statistics

import com.janhafner.myskatemap.apps.trackrecorder.io.data.Location
import com.janhafner.myskatemap.apps.trackrecorder.services.temperature.Temperature
import io.reactivex.Observable
import java.time.temporal.TemporalAccessor

internal final class TrackRecordingStatistic : ITrackRecordingStatistic {
    public override val speed: Statistic = Statistic()

    public override val altitude: Statistic = Statistic()

    public override val ambientTemperature: Statistic = Statistic()

    public override fun addAll(location: List<Location>) {
        if(this.isDestroyed) {
            throw IllegalStateException("Object is destroyed!")
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
            throw IllegalStateException("Object is destroyed!")
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

    public override fun addAmbientTemperature(temperature: Temperature) {
        if(this.isDestroyed) {
            return
        }

        this.ambientTemperature.add(temperature.celsius)
    }

    public override fun addAllAmbientTemperatures(temperatures: List<Temperature>) {
        if(this.isDestroyed) {
            return
        }

        this.ambientTemperature.addAll(temperatures.map { it.celsius })
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