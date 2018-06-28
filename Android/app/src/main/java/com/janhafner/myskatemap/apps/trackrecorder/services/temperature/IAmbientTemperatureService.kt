package com.janhafner.myskatemap.apps.trackrecorder.services.temperature

import com.janhafner.myskatemap.apps.trackrecorder.IDestroyable
import io.reactivex.Observable

internal interface IAmbientTemperatureService : IDestroyable {
    val ambientTemperatureChanged: Observable<Temperature>

    val ambientTemperature: Temperature

    val isListeningChanged: Observable<Boolean>

    val isListening: Boolean

    fun startListening() : Observable<Temperature>

    fun stopListening()
}

