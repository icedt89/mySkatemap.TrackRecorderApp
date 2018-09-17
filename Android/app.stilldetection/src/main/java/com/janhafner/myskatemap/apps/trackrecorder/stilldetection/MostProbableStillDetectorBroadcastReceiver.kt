package com.janhafner.myskatemap.apps.trackrecorder.stilldetection

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.google.android.gms.location.ActivityRecognitionClient
import com.google.android.gms.location.DetectedActivity
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

public final class MostProbableStillDetectorBroadcastReceiver(private val context: Context, private val detectionIntervalInMilliseconds: Int, private val activityRecognitionClient: ActivityRecognitionClient)
    : BroadcastReceiver(),IStillDetector {
    private val stillDetectedSubject: PublishSubject<Boolean> = PublishSubject.create()
    public override val stillDetected: Observable<Boolean> = this.stillDetectedSubject.subscribeOn(Schedulers.computation())

    private val isDetectingChangedSubject: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)
    public override val isDetectingChanged: Observable<Boolean> = this.isDetectingChangedSubject.subscribeOn(Schedulers.computation())

    public override val isDetecting: Boolean
        get() = this.isDetectingChangedSubject.value!!

    private val pendingIntent: PendingIntent

    init {
        val intent = Intent(context, MostProbableDetectedActivityRecognizerIntentService::class.java)
        this.pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    public override fun onReceive(context: Context?, intent: Intent?) {
        if(this.isDestroyed || intent == null || intent.action != MostProbableDetectedActivityRecognizerIntentService.INTENT_ACTION_NAME) {
            return
        }

        val activityType = intent.getIntExtra(MostProbableDetectedActivityRecognizerIntentService.INTENT_EXTRA_TYPE_KEY, DetectedActivity.UNKNOWN)
        if(activityType == DetectedActivity.UNKNOWN) {
            return
        }

        val isStill = activityType == DetectedActivity.STILL

        this.stillDetectedSubject.onNext(isStill)
    }

    public override fun startDetection() {
        if(this.isDestroyed) {
            throw IllegalStateException("Object is destroyed!")
        }

        if(this.isDetecting) {
            throw IllegalStateException("Detection already running!")
        }

        this.context.registerReceiver(this, IntentFilter(MostProbableDetectedActivityRecognizerIntentService.INTENT_ACTION_NAME))

        this.activityRecognitionClient.requestActivityUpdates(this.detectionIntervalInMilliseconds.toLong(), this.pendingIntent)

        this.isDetectingChangedSubject.onNext(true)
    }

    public override fun stopDetection() {
        if(this.isDestroyed) {
            throw IllegalStateException("Object is destroyed!")
        }

        if(!this.isDetecting) {
            throw IllegalStateException("Detection must be started first!")
        }

        this.activityRecognitionClient.removeActivityUpdates(this.pendingIntent)

        this.context.unregisterReceiver(this)

        this.isDetectingChangedSubject.onNext(false)
    }

    private var isDestroyed = false
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