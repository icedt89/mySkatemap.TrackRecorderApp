package com.janhafner.myskatemap.apps.trackrecorder.services.activitydetection

import android.app.IntentService
import android.content.Intent
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity


internal final class ActivityRecognizerIntentService : IntentService("Activity Recognizer Intent Service") {
    public override fun onHandleIntent(intent: Intent?) {
        if(intent == null) {
            return
        }

        val result = ActivityRecognitionResult.extractResult(intent)
        for(detectedActivity in result.probableActivities
                .sortedByDescending {
                    it.confidence
                }) {
            this.broadcastActivity(detectedActivity)
        }
    }

    private fun broadcastActivity(activity: DetectedActivity) {
        val intent = Intent(ActivityRecognizerIntentService.INTENT_ACTION_NAME)
        intent.putExtra(ActivityRecognizerIntentService.INTENT_EXTRA_TYPE_KEY, activity.type)
        intent.putExtra(ActivityRecognizerIntentService.INTENT_EXTRA_CONFIDENCE_KEY, activity.confidence)

        this.sendBroadcast(intent)
    }

    public companion object {
        public const val INTENT_ACTION_NAME = "FROM_ACTIVITY_RECOGNIZER"

        public const val INTENT_EXTRA_TYPE_KEY = "type"

        public const val INTENT_EXTRA_CONFIDENCE_KEY = "confidence"
    }
}