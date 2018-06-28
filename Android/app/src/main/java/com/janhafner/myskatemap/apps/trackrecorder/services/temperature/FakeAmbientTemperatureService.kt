package com.janhafner.myskatemap.apps.trackrecorder.services.temperature

import com.janhafner.myskatemap.apps.trackrecorder.ObservableTimer
import io.reactivex.Observable

internal final class FakeAmbientTemperatureService : IAmbientTemperatureService {
    private val valueGeneratorInterval: ObservableTimer = ObservableTimer()

    public override val ambientTemperatureChanged: Observable<Temperature> = this.valueGeneratorInterval.secondElapsed.map {
        Temperature(it.seconds.toFloat())
    }

    public override val ambientTemperature: Temperature
        get() = Temperature(this.valueGeneratorInterval.secondsElapsed.seconds.toFloat())

    public override val isListeningChanged: Observable<Boolean> = this.valueGeneratorInterval.isRunningChanged

    public override val isListening: Boolean
        get() = this.valueGeneratorInterval.isRunning

    public override fun startListening(): Observable<Temperature> {
        if (this.isDestroyed) {
            throw IllegalStateException("Object is destroyed!")
        }

        if (this.isListening) {
            throw IllegalStateException("Detection already running!")
        }

        this.valueGeneratorInterval.start()

        return this.ambientTemperatureChanged
    }

    public override fun stopListening() {
        if (this.isDestroyed) {
            throw IllegalStateException("Object is destroyed!")
        }

        if (!this.isListening) {
            throw IllegalStateException("Detection must be started first!")
        }

        this.valueGeneratorInterval.stop()
    }

    private var isDestroyed = false
    public override fun destroy() {
        if(this.isDestroyed) {
            return
        }

        this.valueGeneratorInterval.destroy()

        this.isDestroyed = true
    }
}