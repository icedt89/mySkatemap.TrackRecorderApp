package com.janhafner.myskatemap.apps.trackrecorder.stilldetection

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.ActivityRecognitionClient
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionRequest
import com.google.android.gms.location.DetectedActivity

public final class StillDetectorBroadcastReceiver(private val interval: Int, context: Context, private val activityRecognitionClient: ActivityRecognitionClient) : StillDetectorBroadcastReceiverBase() {
    private val pendingIntent: PendingIntent

    private val activityTransitionRequest: ActivityTransitionRequest

    init {
        val intent = Intent(context, ActivityRecognizerIntentService::class.java)
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
        if(this.isDestroyed || intent == null || intent.action != ActivityRecognizerIntentService.INTENT_ACTION_NAME) {
            return
        }

        val activityType = intent.getIntExtra(ActivityRecognizerIntentService.INTENT_EXTRA_ACTIVITY_TYPE_KEY, DetectedActivity.UNKNOWN)
        if (activityType != DetectedActivity.STILL) {
            return
        }

        val transitionType = intent.getIntExtra(ActivityRecognizerIntentService.INTENT_EXTRA_TRANSITION_TYPE_KEY, -1)
        if (transitionType == ActivityTransition.ACTIVITY_TRANSITION_EXIT) {
           this.stillDetectedSubject.onNext(false)
        } else if(transitionType == ActivityTransition.ACTIVITY_TRANSITION_EXIT) {
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

        this.isDetectingChangedSubject.onNext(false)
    }
}