package com.janhafner.myskatemap.apps.trackrecorder.stilldetection

import android.app.IntentService
import android.content.Intent
import com.google.android.gms.location.ActivityTransitionEvent
import com.google.android.gms.location.ActivityTransitionResult

public final class TransitioningActivityRecognizerIntentService : IntentService("Activity Recognizer Intent Service - transitioning to/from STILL") {
    public override fun onHandleIntent(intent: Intent?) {
        if(intent == null) {
            return
        }

        if (ActivityTransitionResult.hasResult(intent)) {
            val result = ActivityTransitionResult.extractResult(intent)!!
            for (transitionEvent in result.transitionEvents) {
                this.broadcastActivity(transitionEvent)
            }
        }
    }

    private fun broadcastActivity(activityTransitionEvent: ActivityTransitionEvent) {
        val intent = Intent(INTENT_ACTION_NAME)
        intent.putExtra(INTENT_EXTRA_TRANSITION_TYPE_KEY, activityTransitionEvent.transitionType)
        intent.putExtra(INTENT_EXTRA_ACTIVITY_TYPE_KEY, activityTransitionEvent.activityType)

        this.sendBroadcast(intent)
    }

    public companion object {
        public const val INTENT_ACTION_NAME = "DETECTED_BY_TRANSITIONING_ACTIVITY_RECOGNIZER"

        public const val INTENT_EXTRA_TRANSITION_TYPE_KEY = "transition-type"

        public const val INTENT_EXTRA_ACTIVITY_TYPE_KEY = "activity-type"
    }
}