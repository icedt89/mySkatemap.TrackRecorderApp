package com.janhafner.myskatemap.apps.trackrecorder.services.activitydetection

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.ActivityRecognitionClient
import com.google.android.gms.location.DetectedActivity
import io.reactivex.Observable

internal final class ActivityDetectorBroadcastReceiver(context: Context, private val activityRecognitionClient: ActivityRecognitionClient)
        : ActivityDetectorBroadcastReceiverBase() {
    private val pendingIntent: PendingIntent

    private val detectedActivityTypeMapping: Map<Int, DetectedActivityType> = mapOf(
            Pair(DetectedActivity.UNKNOWN, DetectedActivityType.Unknown),
            Pair(DetectedActivity.TILTING, DetectedActivityType.Tilting),
            Pair(DetectedActivity.STILL, DetectedActivityType.Still),
            Pair(DetectedActivity.ON_FOOT, DetectedActivityType.OnFoot),
            Pair(DetectedActivity.WALKING, DetectedActivityType.Walking),
            Pair(DetectedActivity.RUNNING, DetectedActivityType.Running),
            Pair(DetectedActivity.ON_BICYCLE, DetectedActivityType.OnBicycle),
            Pair(DetectedActivity.IN_VEHICLE, DetectedActivityType.InVehicle)
    )

    init {
        val intent = Intent(context, ActivityRecognizerIntentService::class.java)
        this.pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    public override fun onReceive(context: Context?, intent: Intent?) {
        if(this.isDestroyed || intent == null || intent.action != ActivityRecognizerIntentService.INTENT_ACTION_NAME) {
            return
        }

        val type = intent.getIntExtra(ActivityRecognizerIntentService.INTENT_EXTRA_TYPE_KEY, DetectedActivity.UNKNOWN)

        val detectedActivityType = this.detectedActivityTypeMapping[type]
        if(detectedActivityType == null) {
            return
        }

        val confidence = intent.getIntExtra(ActivityRecognizerIntentService.INTENT_EXTRA_CONFIDENCE_KEY, 0)
        val detectedActivity = com.janhafner.myskatemap.apps.trackrecorder.services.activitydetection.DetectedActivity(detectedActivityType, confidence)

        this.activityDetectedSubject.onNext(detectedActivity)
    }

    public override fun startDetection(interval: Long) : Observable<com.janhafner.myskatemap.apps.trackrecorder.services.activitydetection.DetectedActivity> {
        if(this.isDestroyed) {
            throw IllegalStateException("Object is destroyed!")
        }

        if(this.isDetecting) {
            throw IllegalStateException("Detection already running!")
        }

        this.activityRecognitionClient.requestActivityUpdates(interval, this.pendingIntent)

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

        this.activityRecognitionClient.removeActivityUpdates(this.pendingIntent)

        this.isDetectingChangedSubject.onNext(false)
    }
}