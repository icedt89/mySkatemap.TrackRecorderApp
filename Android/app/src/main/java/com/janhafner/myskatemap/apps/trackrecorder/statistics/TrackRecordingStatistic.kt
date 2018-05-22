package com.janhafner.myskatemap.apps.trackrecorder.statistics

import com.janhafner.myskatemap.apps.trackrecorder.io.data.Location

internal final class TrackRecordingStatistic {
    public val speed: Statistic = Statistic()

    public val altitude: Statistic = Statistic()

    public fun addAll(location: List<Location>) {
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

    public fun add(location: Location) {
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
}