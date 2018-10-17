package com.janhafner.myskatemap.apps.trackrecorder.activitydetection

import android.util.Log
import com.janhafner.myskatemap.apps.trackrecorder.common.IDestroyable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

public final class ActivityDetectorEmittingSource : IActivityDetectorEmitter, IActivityDetectorSource, IDestroyable {
    private val activityDetectedSubject: PublishSubject<ActivityType> = PublishSubject.create()
    public override val activityDetected: Observable<ActivityType> = this.activityDetectedSubject.subscribeOn(Schedulers.computation())

    public override fun emit(activityType: ActivityType) {
        Log.i("ADBR", "Emitting ${activityType}")

        this.activityDetectedSubject.onNext(activityType)
    }

    private var isDestroyed = false
    public override fun destroy() {
        if (this.isDestroyed) {
            return
        }

        this.activityDetectedSubject.onComplete()

        this.isDestroyed = true

        Log.i("ADBR", "Emitting source destroyed")
    }
}