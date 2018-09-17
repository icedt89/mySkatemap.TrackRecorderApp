package com.janhafner.myskatemap.apps.trackrecorder.stilldetection

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.google.android.gms.location.ActivityRecognitionClient
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionRequest
import com.google.android.gms.location.DetectedActivity
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

public final class TransitioningStillDetectorBroadcastReceiver(private val context: Context, private val activityRecognitionClient: ActivityRecognitionClient)
    : BroadcastReceiver(), IStillDetector {
    private val stillDetectedSubject: PublishSubject<Boolean> = PublishSubject.create()
    public override val stillDetected: Observable<Boolean> = this.stillDetectedSubject.subscribeOn(Schedulers.computation())

    private val isDetectingChangedSubject: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)
    public override val isDetectingChanged: Observable<Boolean> = this.isDetectingChangedSubject.subscribeOn(Schedulers.computation())

    public override val isDetecting: Boolean
        get() = this.isDetectingChangedSubject.value!!

    private val pendingIntent: PendingIntent

    private val activityTransitionRequest: ActivityTransitionRequest

    init {
        val intent = Intent(context, TransitioningActivityRecognizerIntentService::class.java)
        this.pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val enterStillTransition = ActivityTransition.Builder()
                .setActivityType(DetectedActivity.STILL)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build()
        val exitStillTransition = ActivityTransition.Builder()
                .setActivityType(DetectedActivity.STILL)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build()

        this.activityTransitionRequest = ActivityTransitionRequest(listOf(enterStillTransition, exitStillTransition))
    }

    public override fun onReceive(context: Context?, intent: Intent?) {
        if(this.isDestroyed || intent == null || intent.action != TransitioningActivityRecognizerIntentService.INTENT_ACTION_NAME) {
            return
        }

        val activityType = intent.getIntExtra(com.janhafner.myskatemap.apps.trackrecorder.stilldetection.TransitioningActivityRecognizerIntentService.INTENT_EXTRA_ACTIVITY_TYPE_KEY, DetectedActivity.UNKNOWN)
        if (activityType != DetectedActivity.STILL) {
            return
        }

        val transitionType = intent.getIntExtra(TransitioningActivityRecognizerIntentService.INTENT_EXTRA_TRANSITION_TYPE_KEY, -1)
        if (transitionType == ActivityTransition.ACTIVITY_TRANSITION_EXIT) {
           this.stillDetectedSubject.onNext(false)
        } else if(transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
            this.stillDetectedSubject.onNext(true)
        } else{
            return
        }
    }

    public override fun startDetection() {
        if(this.isDestroyed) {
            throw IllegalStateException("Object is destroyed!")
        }

        if(this.isDetecting) {
            throw IllegalStateException("Detection already running!")
        }

        this.context.registerReceiver(this, IntentFilter(TransitioningActivityRecognizerIntentService.INTENT_ACTION_NAME))

        this.activityRecognitionClient.requestActivityTransitionUpdates(activityTransitionRequest, this.pendingIntent)

        this.isDetectingChangedSubject.onNext(true)
    }

    public override fun stopDetection() {
        if(this.isDestroyed) {
            throw IllegalStateException("Object is destroyed!")
        }

        if(!this.isDetecting) {
            throw IllegalStateException("Detection must be started first!")
        }

        this.activityRecognitionClient.removeActivityTransitionUpdates(this.pendingIntent)

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