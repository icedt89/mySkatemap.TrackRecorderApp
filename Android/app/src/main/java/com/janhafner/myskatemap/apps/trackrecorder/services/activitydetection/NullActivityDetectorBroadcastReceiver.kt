package com.janhafner.myskatemap.apps.trackrecorder.services.activitydetection

import android.content.Context
import android.content.Intent
import io.reactivex.Observable

internal final class NullActivityDetectorBroadcastReceiver : ActivityDetectorBroadcastReceiverBase() {
    public override fun onReceive(context: Context?, intent: Intent?) {
    }

    public override fun startDetection(interval: Long): Observable<DetectedActivity> {
        if(this.isDestroyed) {
            throw IllegalStateException("Object is destroyed!")
        }

        if(this.isDetecting) {
            throw IllegalStateException("Detection already running!")
        }

        this.isDetectingChangedSubject.onNext(true)

        return this.activityDetected
    }

    public override fun stopDetection() {
        if(this.isDestroyed) {
            throw IllegalStateException("Object is destroyed!")
        }

        if(!this.isDetecting) {
            throw IllegalStateException("Detection must be started first!")
        }

        this.isDetectingChangedSubject.onNext(false)
    }
}