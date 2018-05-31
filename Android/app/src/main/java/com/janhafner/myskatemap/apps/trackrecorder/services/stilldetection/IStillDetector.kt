package com.janhafner.myskatemap.apps.trackrecorder.services.stilldetection

import com.janhafner.myskatemap.apps.trackrecorder.IDestroyable
import io.reactivex.Observable

internal interface IStillDetector : IDestroyable {
    val stillDetectedChanged: Observable<Boolean>

    val isStill: Boolean

    val isDetectingChanged: Observable<Boolean>

    val isDetecting: Boolean

    fun startDetection(interval: Long) : Observable<Boolean>

    fun stopDetection()
}