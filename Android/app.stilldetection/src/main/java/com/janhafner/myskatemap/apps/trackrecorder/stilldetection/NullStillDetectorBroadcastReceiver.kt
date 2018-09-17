package com.janhafner.myskatemap.apps.trackrecorder.stilldetection

import io.reactivex.Observable

public final class NullStillDetectorBroadcastReceiver : IStillDetector {
    public override val stillDetected: Observable<Boolean> = Observable.never()

    public override val isDetectingChanged: Observable<Boolean>  = Observable.never()

    public override val isDetecting: Boolean  = false

    public override fun startDetection() {
    }

    public override fun stopDetection() {
    }

    public override fun destroy() {
    }
}