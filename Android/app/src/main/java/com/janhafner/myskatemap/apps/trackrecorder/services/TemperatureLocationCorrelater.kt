package com.janhafner.myskatemap.apps.trackrecorder.services

import android.util.Log
import com.janhafner.myskatemap.apps.trackrecorder.io.data.Location
import com.janhafner.myskatemap.apps.trackrecorder.services.temperature.Temperature
import org.joda.time.DateTime
import org.joda.time.Period

internal final class TemperatureLocationCorrelater(private val tolerance: Period) {
    init {
        Log.v("TLC", tolerance.seconds.toString())
    }
    /*
    Note: If there are more than one temperatures which could be correlated to one location,
    only the one with the smallest difference, in the tolerance window, will be taken,

Locations (time line)
---1--2--3----4-----5---------6----7----8----9-----------------10------------11---12--------13------

Temperatures (time line)
---a------b------c-d-e----f---g----h----i--jk------l----m--n-o---------p---q-r--st--u------v-----w--

Correlated result (marker represents location)
--1a----3b--4c----5d---------6g---7h---8i---8j-----------------10o----------11r---12u-----12v------
     */
    public fun correlateAll(locations: List<Location>, temperatures: List<Temperature>) {
        // Optimization: Loop over all locations + temperatures in timewindow and move the timewindow forward by the end time of the last processed location
        for (location in locations) {
            val timewindow = Timewindow(location.capturedAt, this.tolerance)

            val temperaturesInTimewindow = temperatures.map {
                PartialCorrelationResult(location, it, timewindow.isInTimewindow(it.capturedAt))
            }.filter{
                it.timewindowDifference != null
            }

            val closestTemperature = temperaturesInTimewindow.minBy {
                it.timewindowDifference!!.seconds
            }

            if (closestTemperature != null) {
                location.correlatingAmbientTemperature = closestTemperature.temperature
            }
        }
    }

    private final class PartialCorrelationResult(public val location: Location, public val temperature: Temperature, public val timewindowDifference: Period?) {
    }

    private final class Timewindow(public val current: DateTime, public val tolerance: Period) {
        public val lowerBound: DateTime = this.current.minus(tolerance)

        public val upperAllowed: DateTime = this.current.plus(tolerance)

        public fun isInTimewindow(at: DateTime) : Period? {
            val isInTimewindow = at >= this.lowerBound && at <= this.upperAllowed
            if(!isInTimewindow) {
                return null
            }

            return Period(this.current, at)
        }
    }
}