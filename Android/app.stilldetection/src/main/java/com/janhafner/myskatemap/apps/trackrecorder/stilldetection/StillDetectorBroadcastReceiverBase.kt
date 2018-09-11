package com.janhafner.myskatemap.apps.trackrecorder.stilldetection

import android.content.BroadcastReceiver
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

public abstract class StillDetectorBroadcastReceiverBase : BroadcastReceiver(), IStillDetector {
    protected val stillDetectedSubject: PublishSubject<Boolean> = PublishSubject.create()
    public override val stillDetected: Observable<Boolean> = this.stillDetectedSubject

    protected val isDetectingChangedSubject: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)
    public override val isDetectingChanged: Observable<Boolean> = this.isDetectingChangedSubject

    public override val isDetecting: Boolean
        get() = this.isDetectingChangedSubject.value!!

    protected var isDestroyed = false
    public override fun destroy() {
        if (this.isDestroyed) {
            return
        }

        if (this.isDetecting) {
            this.stopDetection()
        }

        this.isDetectingChangedSubject.onComplete()
        this.stillDetectedSubject.onComplete()

        this.isDestroyed = true
    }
}