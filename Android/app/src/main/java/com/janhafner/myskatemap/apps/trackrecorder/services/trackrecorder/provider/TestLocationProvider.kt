package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.provider

import android.content.Context
import android.os.SystemClock
import com.google.android.gms.maps.model.LatLng
import com.janhafner.myskatemap.apps.trackrecorder.clone
import com.janhafner.myskatemap.apps.trackrecorder.isLocationServicesEnabled
import com.janhafner.myskatemap.apps.trackrecorder.location.Location
import org.joda.time.DateTime
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.collections.ArrayList

internal final class TestLocationProvider(private val context: Context,
                                          private val initialLocation: Location,
                                          private val bearingStepping: Float = 0.0001f,
                                          private val latitudeStepping: Double = 0.001,
                                          private val longitudeStepping: Double = 0.001,
                                          private val delay: Long = 0,
                                          private val interval: Long = 5000,
                                          private val simulateDependencyToAndroidLocationServices: Boolean = true): LocationProvider() {
    private val timer: Timer = Timer()

    private val genericCoordinates: MutableList<PointD> = ArrayList()

    private var lastComputedLocation: Location? = null

    private var postLocationTimerTask: TimerTask? = this.createTimerTask()

    private fun createTimerTask(): TimerTask {
        return object: TimerTask() {
            override fun run() {
                val self = this@TestLocationProvider

                if (self.simulateDependencyToAndroidLocationServices && !self.context.isLocationServicesEnabled()) {
                    return
                }

                val computedLocation = self.computeLocation()

                self.publishLocationUpdate(computedLocation)
            }
        }
    }

    public override fun overrideSequenceNumber(sequenceNumber: Int) {
        super.overrideSequenceNumber(-1)
        this.lastComputedLocation = null

        this.genericCoordinates.clear()

        for (i in 0..sequenceNumber) {
            this.computeLocation()
        }

        super.overrideSequenceNumber(sequenceNumber)
    }

    private fun computeNextLocation(counter: Int, initialLocation: Location, offsetLatitude: Double, offsetLongitude: Double): LatLng {
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

        return LatLng(x + initialLocation.latitude, y + initialLocation.longitude)
    }

    private fun computeLocation(): Location {
        val sequenceNumber = this.generateSequenceNumber()

        if (this.lastComputedLocation == null) {
            this.lastComputedLocation = Location(sequenceNumber)

            this.lastComputedLocation?.provider = "fake-gps"
            this.lastComputedLocation?.bearing = initialLocation.bearing
            this.lastComputedLocation?.accuracy = initialLocation.accuracy
            this.lastComputedLocation?.capturedAt = DateTime.now()
            this.lastComputedLocation?.latitude = initialLocation.latitude
            this.lastComputedLocation?.longitude = initialLocation.longitude
            this.lastComputedLocation?.speed = initialLocation.speed

            this.genericCoordinates.add(PointD(0.0, 0.0))
        } else {
            this.lastComputedLocation = this.lastComputedLocation?.clone(sequenceNumber)

            this.lastComputedLocation?.bearing = this.lastComputedLocation?.bearing?.plus(this.bearingStepping)

            val nextComputedLocation = this.computeNextLocation(sequenceNumber, this.initialLocation, this.latitudeStepping, this.longitudeStepping)

            this.lastComputedLocation?.latitude = nextComputedLocation.latitude
            this.lastComputedLocation?.longitude = nextComputedLocation.longitude
        }

        return this.lastComputedLocation!!
    }

    public override fun getCurrentLocation(): Location {
        val result = Location(-1)

        result.latitude = ThreadLocalRandom.current().nextDouble() * 50
        if(SystemClock.elapsedRealtimeNanos() % 2 == 0L) {
            result.latitude = -result.latitude
        }

        result.longitude = ThreadLocalRandom.current().nextDouble() * 12
        if(SystemClock.elapsedRealtimeNanos() % 4 == 0L) {
            result.longitude = -result.longitude
        }

        return result
    }

    override fun stopLocationUpdates() {
        if (!this.isActive) {
            throw IllegalStateException()
        }

        this.postLocationTimerTask!!.cancel()
        this.postLocationTimerTask = null

        this.isActive = false
    }

    override fun startLocationUpdates() {
        if (this.isActive) {
            throw IllegalStateException()
        }

        if (this.simulateDependencyToAndroidLocationServices && !this.context.isLocationServicesEnabled()) {
            throw IllegalStateException("Location Services are disabled!")
        }

        if (this.postLocationTimerTask == null) {
            this.postLocationTimerTask = this.createTimerTask()
        }

        this.timer.schedule(this.postLocationTimerTask, this.delay, this.interval)

        this.isActive = true
    }

    private final class PointD(public val x: Double, public val y: Double) {
        public override fun toString(): String {
            return "PointD(X: ${this.x}; Y: ${this.y})"
        }
    }
}