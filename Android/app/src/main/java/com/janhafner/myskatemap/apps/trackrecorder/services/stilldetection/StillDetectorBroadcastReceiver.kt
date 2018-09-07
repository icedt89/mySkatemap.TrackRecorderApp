package com.janhafner.myskatemap.apps.trackrecorder.services.stilldetection

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.ActivityRecognitionClient
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject

internal final class StillDetectorBroadcastReceiver(context: Context, private val activityRecognitionClient: ActivityRecognitionClient)
        : BroadcastReceiver(), IStillDetector {
    private val pendingIntent: PendingIntent

    private val stillDetectedChangedSubject: BehaviorSubject<Boolean> = BehaviorSubject.create<Boolean>()
    public override val stillDetectedChanged: Observable<Boolean> = this.stillDetectedChangedSubject.subscribeOn(Schedulers.computation())

    public override val isStill: Boolean
        get() = this.stillDetectedChangedSubject.value

    private val isDetectingChangedSubject: BehaviorSubject<Boolean> = BehaviorSubject.createDefault<Boolean>(false)
    public override val isDetectingChanged: Observable<Boolean> = this.isDetectingChangedSubject.subscribeOn(Schedulers.computation())

    public override val isDetecting: Boolean
        get() = this.isDetectingChangedSubject.value

    init {
        val intent = Intent(context, ActivityRecognizerIntentService::class.java)
        this.pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    public override fun onReceive(context: Context?, intent: Intent?) {
        if(this.isDestroyed || intent == null || intent.action != ActivityRecognizerIntentService.INTENT_ACTION_NAME) {
            return
        }

        if(ActivityRecognitionResult.hasResult(intent)) {
            val activityRecognitionResult = ActivityRecognitionResult.extractResult(intent)
            if(activityRecognitionResult.mostProbableActivity.type == DetectedActivity.STILL) {
                this@StillDetectorBroadcastReceiver.stillDetectedChangedSubject.onNext(true)
            } else if(activityRecognitionResult.mostProbableActivity.type != DetectedActivity.UNKNOWN) {
                this@StillDetectorBroadcastReceiver.stillDetectedChangedSubject.onNext(false)
            }
        }
    }

    public override fun startDetection(interval: Long) : Observable<Boolean> {
        if(this.isDestroyed) {
            throw IllegalStateException("Object is destroyed!")
        }

        if(this.isDetecting) {
            throw IllegalStateException("Detection already running!")
        }

        this.activityRecognitionClient.requestActivityUpdates(interval, this.pendingIntent)

        this.isDetectingChangedSubject.onNext(!this.isDetecting)

        return this.stillDetectedChanged
    }

    public override fun stopDetection() {
        if(this.isDestroyed) {
            throw IllegalStateException("Object is destroyed!")
        }

        if(!this.isDetecting) {
            throw IllegalStateException("Detection must be started first!")
        }

        this.activityRecognitionClient.removeActivityUpdates(this.pendingIntent)

        this.isDetectingChangedSubject.onNext(!this.isDetecting)
    }

    private var isDestroyed: Boolean = false
    public override fun destroy() {
        if(this.isDestroyed) {
            return
        }

        this.isDetectingChangedSubject.onComplete()
        this.stillDetectedChangedSubject.onComplete()

        this.isDestroyed = true
    }
}