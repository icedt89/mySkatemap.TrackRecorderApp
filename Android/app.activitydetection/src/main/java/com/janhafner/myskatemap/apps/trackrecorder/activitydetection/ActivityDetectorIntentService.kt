package com.janhafner.myskatemap.apps.trackrecorder.activitydetection

import android.app.IntentService
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity

public final class ActivityDetectorIntentService : IntentService("Activity Detector Intent Service") {
    public override fun onHandleIntent(intent: Intent?) {
        Log.i("ADBR", "Intent service handles intent ${intent}")

        if(intent == null) {
            return
        }

        if (ActivityRecognitionResult.hasResult(intent)) {
            Log.i("ADBR", "Intent holds activity recognition data")

            val result = ActivityRecognitionResult.extractResult(intent)

            val mostProbableActivity = result.mostProbableActivity

            Log.i("ADBR", "Most probable activity is ${mostProbableActivity.type} ${mostProbableActivity.confidence}")

            this.broadcastActivity(mostProbableActivity)
        } else {
            Log.i("ADBR", "Intent does not hold any activity recognition data")
        }
    }

    private fun broadcastActivity(activity: DetectedActivity) {
        val intent = Intent(INTENT_ACTION_NAME)
        intent.putExtra(ActivityDetectorIntentService.INTENT_EXTRA_TYPE_KEY, activity.type)
        intent.putExtra(ActivityDetectorIntentService.INTENT_EXTRA_CONFIDENCE_KEY, activity.confidence)

        Log.i("ADBR", "Broadcasting activity ${activity.type} ${activity.confidence}")

        this.sendBroadcast(intent)
    }

    public companion object {
        public const val INTENT_ACTION_NAME = "DETECTED_BY_ACTIVITY_DETECTOR"

        public const val INTENT_EXTRA_TYPE_KEY = "type"

        public const val INTENT_EXTRA_CONFIDENCE_KEY = "confidence"
    }
}