package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider

import android.content.Context
import com.janhafner.myskatemap.apps.trackrecorder.common.ObjectDestroyedException
import com.janhafner.myskatemap.apps.trackrecorder.common.types.Location
import com.janhafner.myskatemap.apps.trackrecorder.isLocationServicesEnabled
import com.janhafner.myskatemap.apps.trackrecorder.map.MapLocation
import org.joda.time.DateTime
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random

internal final class SimulatedLocationProvider(private val context: Context,
                                               private val initialLocation: Location,
                                               private val bearingStepping: Float = 0.0001f,
                                               private val latitudeStepping: Double = 0.001,
                                               private val longitudeStepping: Double = 0.001,
                                               private val delay: Int = 0,
                                               private val interval: Int = 5000): LocationProvider() {
    private val timer: Timer = Timer()

    private val genericCoordinates: MutableList<PointD> = ArrayList()

    private var lastComputedLocation: Location? = null

    private var postLocationTimerTask: TimerTask? = this.createTimerTask()

    private var sequenceNumber: Int = -1

    private fun createTimerTask(): TimerTask {
        return object: TimerTask() {
            override fun run() {
                val self = this@SimulatedLocationProvider

                self.context.isLocationServicesEnabled()
                        .onErrorReturn { false }
                        .filter { it }
                        .subscribe {
                            val computedLocation = self.computeLocation()

                            self.publishLocationUpdate(computedLocation)
                        }
            }
        }
    }

    private fun computeNextLocation(counter: Int, initialLocation: Location, offsetLatitude: Double, offsetLongitude: Double): MapLocation {
        val previousCoordinates = genericCoordinates[counter - 1]

        var x = previousCoordinates.x
        var y = previousCoordinates.y

        if (counter % 2 != 0) {
            if (x > 0) {
                x = -x
            } else if (x <= 0) {
                x = -x + offsetLatitude
            }
        } else {
            if (y >= 0) {
                y = -y - offsetLongitude
            } else if (y < 0) {
                y = -y
            }
        }

        genericCoordinates.add(PointD(x, y))

        return MapLocation(x + initialLocation.latitude, y + initialLocation.longitude)
    }

    private fun computeLocation(): Location {
        val sequenceNumber = ++this.sequenceNumber

        if (this.lastComputedLocation == null) {
            val location = Location("fake-gps", DateTime.now(), initialLocation.latitude, initialLocation.longitude)

            location.bearing = initialLocation.bearing
            location.accuracy = initialLocation.accuracy
            location.altitude = initialLocation.altitude
            location.speed = initialLocation.speed

            this.lastComputedLocation = location

            this.genericCoordinates.add(PointD(0.0, 0.0))
        } else {
            val clonedLastComputedLocation = this.cloneLocation(this.lastComputedLocation!!)

            clonedLastComputedLocation.bearing = clonedLastComputedLocation.bearing?.plus(this.bearingStepping)

            val nextComputedLocation = this.computeNextLocation(sequenceNumber, this.initialLocation, this.latitudeStepping, this.longitudeStepping)

            clonedLastComputedLocation.latitude = nextComputedLocation.latitude
            clonedLastComputedLocation.longitude = nextComputedLocation.longitude

            clonedLastComputedLocation.speed = this.computeSpeed(sequenceNumber).toFloat()
            clonedLastComputedLocation.altitude = this.computeAltitude(sequenceNumber)

            this.lastComputedLocation = clonedLastComputedLocation
        }

        return this.lastComputedLocation!!
    }

    private fun cloneLocation(location: Location): Location {
        val result = Location(location.provider, DateTime.now(), location.latitude, location.longitude)

        result.bearing = location.bearing
        result.speed = location.speed
        result.accuracy = location.accuracy
        result.bearingAccuracyDegrees = location.bearingAccuracyDegrees
        result.speedAccuracyMetersPerSecond = location.speedAccuracyMetersPerSecond
        result.verticalAccuracyMeters = location.verticalAccuracyMeters
        result.altitude = location.altitude
        result.segmentNumber = location.segmentNumber

        return result
    }

    private fun computeSpeed(sequenceNumber: Int): Double {
        val period = 24
        val maximum = 50

        val result = ((1 - Math.sin(sequenceNumber * 2 * Math.PI / period)) * (maximum / 2) + (maximum / 2)) * maximum

        var offset = Random.nextDouble()
        if((android.os.SystemClock.uptimeMillis() % 2) == 0.toLong()) {
            offset = -offset
        }

        return result + offset
    }

    private fun computeAltitude(sequenceNumber: Int): Double {
        val period = 24
        val maximum = 100

        val result =  (Math.sin(sequenceNumber * 2 * Math.PI / period) * (maximum / 2) + (maximum / 2)) * maximum

        var offset = Random.nextDouble()
        if((android.os.SystemClock.uptimeMillis() % 2) == 0.toLong()) {
            offset = -offset
        }

        return result + offset
    }

    public override fun stopLocationUpdates() {
        if(this.isDestroyed) {
            throw ObjectDestroyedException()
        }

        if (!this.isActive) {
            throw IllegalStateException("LocationProvider must be started first!")
        }

        this.postLocationTimerTask!!.cancel()
        this.postLocationTimerTask = null

        this.isActive = false
    }

    public override fun startLocationUpdates() {
        if(this.isDestroyed) {
            throw ObjectDestroyedException()
        }

        if (this.isActive) {
            throw IllegalStateException("LocationProvider must be stopped first!")
        }

        this.context.isLocationServicesEnabled()
                .onErrorReturn { false }
                .subscribe {
                    result ->
                    if(result) {
                        if (this.postLocationTimerTask == null) {
                            this.postLocationTimerTask = this.createTimerTask()
                        }

                        this.timer.schedule(this.postLocationTimerTask, this.delay.toLong(), this.interval.toLong())

                        this.isActive = true
                    } else {
                        throw IllegalStateException("Location Services are disabled!")
                    }
                }
    }

    protected final override fun destroyCore() {
        if(this.isDestroyed) {
            return
        }

        this.postLocationTimerTask?.cancel()
        this.timer.cancel()
    }

    private final class PointD(public val x: Double, public val y: Double) {
    }
}