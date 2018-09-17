package com.janhafner.myskatemap.apps.trackrecorder.stilldetection

import android.app.IntentService
import android.content.Intent
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity

public final class MostProbableDetectedActivityRecognizerIntentService : IntentService("Activity Recognizer Intent Service - most probable detected activity") {
    public override fun onHandleIntent(intent: Intent?) {
        if(intent == null) {
            return
        }

        if (ActivityRecognitionResult.hasResult(intent)) {
            val result = ActivityRecognitionResult.extractResult(intent)
            val mostProbableActivity = result.mostProbableActivity

            this.broadcastActivity(mostProbableActivity)
        }
    }

    private fun broadcastActivity(activity: DetectedActivity) {
        val intent = Intent(INTENT_ACTION_NAME)
        intent.putExtra(MostProbableDetectedActivityRecognizerIntentService.INTENT_EXTRA_TYPE_KEY, activity.type)
        intent.putExtra(MostProbableDetectedActivityRecognizerIntentService.INTENT_EXTRA_CONFIDENCE_KEY, activity.confidence)

        this.sendBroadcast(intent)
    }

    public companion object {
        public const val INTENT_ACTION_NAME = "DETECTED_BY_MOST_PROBABLE_DETECTED_ACTIVITY_RECOGNIZER"

        public const val INTENT_EXTRA_TYPE_KEY = "type"

        public const val INTENT_EXTRA_CONFIDENCE_KEY = "confidence"
    }
}