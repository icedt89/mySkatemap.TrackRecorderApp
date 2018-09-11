package com.janhafner.myskatemap.apps.trackrecorder.stilldetection

import com.janhafner.myskatemap.apps.trackrecorder.common.IDestroyable
import io.reactivex.Observable


public interface IStillDetector : IDestroyable {
    val stillDetected : Observable<Boolean>

    val isDetectingChanged : Observable<Boolean>

    val isDetecting : Boolean

    fun startDetection()

    fun stopDetection()
}

