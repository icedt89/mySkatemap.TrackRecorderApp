package com.janhafner.myskatemap.apps.trackrecorder.services.activitydetection

import com.janhafner.myskatemap.apps.trackrecorder.common.IDestroyable
import io.reactivex.Observable

internal interface IActivityDetector : IDestroyable {
    val activityDetected: Observable<DetectedActivity>

    val isDetectingChanged: Observable<Boolean>

    val isDetecting: Boolean

    fun startDetection(interval: Long) : Observable<DetectedActivity>

    fun stopDetection()
}