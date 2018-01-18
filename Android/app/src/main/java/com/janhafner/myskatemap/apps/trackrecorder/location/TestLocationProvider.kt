package com.janhafner.myskatemap.apps.trackrecorder.location

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.janhafner.myskatemap.apps.trackrecorder.clone
import com.janhafner.myskatemap.apps.trackrecorder.toLatLng
import org.joda.time.DateTime
import java.util.*

internal final class TestLocationProvider(private val initialLocation : Location,
                                          private val bearingStepping : Float = 0.01f,
                                          private val latitudeStepping : Double = 0.01,
                                          private val longitudeStepping : Double = 0.01,
                                          private val delay : Long = 5000,
                                          private val interval : Long = 5000) : LocationProvider() {
    private val timer : Timer = Timer()

    private var lastComputedLocation : com.janhafner.myskatemap.apps.trackrecorder.location.Location? = null

    private var postLocationTimerTask : TimerTask? = null

    private fun createTimerTask() : TimerTask {
        return object : TimerTask() {
            override fun run() {
                val self = this@TestLocationProvider

                val computedLocation = self.computeLocation()

                Log.i("TestLocationProvider", computedLocation.toString())

                self.postLocationUpdate(computedLocation)
            }
        }
    }

    private fun computeNextLocation(counter : Int, initialLocation: LatLng, currentLocation : LatLng, offsetLatitude: Double, offsetLongitude : Double) : LatLng {
        if(counter == 0) {
            return initialLocation
        }

        var x = currentLocation.latitude
        var y = currentLocation.longitude

        // TODO: Place code here which computes funny figure to draw on map.

        x += offsetLatitude
        y += offsetLongitude

        return LatLng(x, y)
    }

    private fun computeLocation() : com.janhafner.myskatemap.apps.trackrecorder.location.Location {
        val sequenceNumber = this.generateSequenceNumber()

        if(this.lastComputedLocation == null) {
            this.lastComputedLocation = Location(sequenceNumber)

            this.lastComputedLocation!!.provider = "fake-gps"
            this.lastComputedLocation!!.bearing = initialLocation.bearing
            this.lastComputedLocation!!.accuracy = initialLocation.accuracy
            this.lastComputedLocation!!.capturedAt = DateTime.now()
            this.lastComputedLocation!!.latitude = initialLocation.latitude
            this.lastComputedLocation!!.longitude = initialLocation.longitude
            this.lastComputedLocation!!.speed = initialLocation.speed
        } else {
            this.lastComputedLocation = this.lastComputedLocation!!.clone(sequenceNumber)

            this.lastComputedLocation!!.bearing = this.lastComputedLocation!!.bearing?.plus(this.bearingStepping)

            val nextComputedLocation = this.computeNextLocation(sequenceNumber, this.initialLocation.toLatLng(), this.lastComputedLocation!!.toLatLng(), this.latitudeStepping, this.longitudeStepping)

            this.lastComputedLocation!!.latitude = nextComputedLocation.latitude
            this.lastComputedLocation!!.longitude = nextComputedLocation.longitude
        }

        return this.lastComputedLocation!!
    }

    override fun stopLocationUpdates() {
        if(this.postLocationTimerTask == null) {
            throw IllegalStateException()
        }

        this.postLocationTimerTask!!.cancel()
        this.postLocationTimerTask = null

        this.isActive = false
    }

    override fun startLocationUpdates() {
        if(this.postLocationTimerTask != null)  {
            throw IllegalStateException()
        }

        this.postLocationTimerTask = this.createTimerTask()

        this.timer.schedule(this.postLocationTimerTask, this.delay, this.interval)

        this.isActive = true
    }
}