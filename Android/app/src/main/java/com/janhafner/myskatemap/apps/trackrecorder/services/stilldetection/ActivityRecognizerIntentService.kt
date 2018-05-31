package com.janhafner.myskatemap.apps.trackrecorder.services.stilldetection

import android.app.IntentService
import android.content.Intent

internal final class ActivityRecognizerIntentService : IntentService("Activity Recognizer Intent Service") {
    override fun onHandleIntent(intent: Intent?) {
        if(intent == null) {
            return
        }

        intent.action = INTENT_ACTION_NAME

        this.sendBroadcast(intent)
    }

    companion object {
        public const val INTENT_ACTION_NAME = "FROM_ACTIVITY_RECOGNIZER"
    }
}