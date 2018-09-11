package com.janhafner.myskatemap.apps.trackrecorder.services.activitydetection

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

internal abstract class ActivityDetectorBroadcastReceiverBase : BroadcastReceiver(), IActivityDetector {
    protected val activityDetectedSubject: BehaviorSubject<DetectedActivity> = BehaviorSubject.create()
    public override val activityDetected: Observable<DetectedActivity> = this.activityDetectedSubject

    protected val isDetectingChangedSubject: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)
    public override val isDetectingChanged: Observable<Boolean> = this.isDetectingChangedSubject

    public override val isDetecting: Boolean
        get() = this.isDetectingChangedSubject.value!!

    public override fun onReceive(context: Context?, intent: Intent?) {
    }

    public abstract override fun startDetection(interval: Long): Observable<DetectedActivity>

    public abstract override fun stopDetection()

    protected var isDestroyed = false
    public override fun destroy() {
        if (this.isDestroyed) {
            return
        }

        if(this.isDetecting) {
            this.stopDetection()
        }

        this.isDetectingChangedSubject.onComplete()
        this.activityDetectedSubject.onComplete()

        this.isDestroyed = true
    }
}