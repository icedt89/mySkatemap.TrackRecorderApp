package com.janhafner.myskatemap.apps.trackrecorder.location.provider

import android.content.Context
import android.provider.Settings
import com.google.android.gms.maps.model.LatLng
import com.janhafner.myskatemap.apps.trackrecorder.clone
import com.janhafner.myskatemap.apps.trackrecorder.location.Location
import com.janhafner.myskatemap.apps.trackrecorder.toLatLng
import org.joda.time.DateTime
import java.util.*

internal final class TestLocationProvider(private val context : Context,
                                          private val initialLocation : Location,
                                          private val bearingStepping : Float = 0.01f,
                                          private val latitudeStepping : Double = 0.01,
                                          private val longitudeStepping : Double = 0.01,
                                          private val delay : Long = 5000,
                                          private val interval : Long = 5000,
                                          private val simulateDependencyToAndroidLocationServices : Boolean = true) : LocationProvider() {
    private val timer : Timer = Timer()

    private var lastComputedLocation : Location? = null

    private var postLocationTimerTask : TimerTask? = this.createTimerTask()

    private fun createTimerTask() : TimerTask {
        return object : TimerTask() {
            override fun run() {
                val self = this@TestLocationProvider

                if(self.simulateDependencyToAndroidLocationServices && !self.isLocationServicesEnabled()) {
                    return
                }

                val computedLocation = self.computeLocation()

                self.postLocationUpdate(computedLocation)
            }
        }
    }

    private fun isLocationServicesEnabled() : Boolean {
        val contentResolver = this.context.contentResolver

        return Settings.Secure.getInt(contentResolver, Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF) != Settings.Secure.LOCATION_MODE_OFF
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

    private fun computeLocation() : Location {
        val sequenceNumber = this.generateSequenceNumber()

        if(this.lastComputedLocation == null) {
            this.lastComputedLocation = Location(sequenceNumber)

            this.lastComputedLocation?.provider = "fake-gps"
            this.lastComputedLocation?.bearing = initialLocation.bearing
            this.lastComputedLocation?.accuracy = initialLocation.accuracy
            this.lastComputedLocation?.capturedAt = DateTime.now()
            this.lastComputedLocation?.latitude = initialLocation.latitude
            this.lastComputedLocation?.longitude = initialLocation.longitude
            this.lastComputedLocation?.speed = initialLocation.speed
        } else {
            this.lastComputedLocation = this.lastComputedLocation?.clone(sequenceNumber)

            this.lastComputedLocation?.bearing = this.lastComputedLocation?.bearing?.plus(this.bearingStepping)

            val nextComputedLocation = this.computeNextLocation(sequenceNumber, this.initialLocation.toLatLng(), this.lastComputedLocation!!.toLatLng(), this.latitudeStepping, this.longitudeStepping)

            this.lastComputedLocation?.latitude = nextComputedLocation.latitude
            this.lastComputedLocation?.longitude = nextComputedLocation.longitude
        }

        return this.lastComputedLocation!!
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

        if(this.simulateDependencyToAndroidLocationServices && !this.isLocationServicesEnabled()) {
            throw IllegalStateException()
        }

        if(this.postLocationTimerTask == null) {
            this.postLocationTimerTask = this.createTimerTask()
        }

        this.timer.schedule(this.postLocationTimerTask, this.delay, this.interval)

        this.isActive = true
    }
}