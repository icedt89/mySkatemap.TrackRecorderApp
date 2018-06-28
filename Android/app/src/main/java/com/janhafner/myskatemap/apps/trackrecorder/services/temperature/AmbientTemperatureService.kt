package com.janhafner.myskatemap.apps.trackrecorder.services.temperature

import android.hardware.*
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

internal final class AmbientTemperatureService(private val sensorManager: SensorManager, private val ambientTemperatureSensor: Sensor) : SensorEventListener, IAmbientTemperatureService {
    private val ambientTemperatureChangedSubject: BehaviorSubject<Temperature> = BehaviorSubject.createDefault(Temperature.empty)
    public override val ambientTemperatureChanged: Observable<Temperature> = this.ambientTemperatureChangedSubject

    public override val ambientTemperature: Temperature
        get() = this.ambientTemperatureChangedSubject.value

    private val isListeningChangedSubject: BehaviorSubject<Boolean> = BehaviorSubject.createDefault<Boolean>(false)
    public override val isListeningChanged: Observable<Boolean> = this.isListeningChangedSubject

    public override val isListening: Boolean
        get() = this.isListeningChangedSubject.value

    public override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    public override fun onSensorChanged(event: SensorEvent?) {
        val celsius = event!!.values[0]

        this.ambientTemperatureChangedSubject.onNext(Temperature(celsius))
    }

    public override fun startListening() : Observable<Temperature> {
        if(this.isDestroyed) {
            throw IllegalStateException("Object is destroyed!")
        }

        if(this.isListening) {
            throw IllegalStateException("Detection already running!")
        }

        this.sensorManager.registerListener(this, this.ambientTemperatureSensor, SensorManager.SENSOR_DELAY_NORMAL)

        this.isListeningChangedSubject.onNext(!this.isListening)

        return this.ambientTemperatureChanged
    }

    public override fun stopListening() {
        if(this.isDestroyed) {
            throw IllegalStateException("Object is destroyed!")
        }

        if(!this.isListening) {
            throw IllegalStateException("Detection must be started first!")
        }

        this.sensorManager.unregisterListener(this)

        this.isListeningChangedSubject.onNext(!this.isListening)
    }

    private var isDestroyed: Boolean = false
    public override fun destroy() {
        if(this.isDestroyed) {
            return
        }

        this.ambientTemperatureChangedSubject.onComplete()

        this.isDestroyed = true
    }
}