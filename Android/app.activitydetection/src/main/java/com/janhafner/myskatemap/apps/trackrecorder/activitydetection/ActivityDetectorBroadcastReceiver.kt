package com.janhafner.myskatemap.apps.trackrecorder.activitydetection

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.google.android.gms.location.ActivityRecognitionClient
import com.google.android.gms.location.DetectedActivity
import com.janhafner.myskatemap.apps.trackrecorder.common.IDestroyable
import com.janhafner.myskatemap.apps.trackrecorder.common.ObjectDestroyedException

public final class ActivityDetectorBroadcastReceiver(private val context: Context,
                                                     private val detectionIntervalInMilliseconds: Int,
                                                     private val activityRecognitionClient: ActivityRecognitionClient,
                                                     private val activityDetectorEmitter: IActivityDetectorEmitter)
    : BroadcastReceiver(), IDestroyable {
    private val pendingIntent: PendingIntent

    public var isDetecting = false
        private set

    private val activityTypeMapping: Map<Int, ActivityType> = mapOf(
            Pair(0, ActivityType.InVehicle),
            Pair(1, ActivityType.OnBicycle),
            Pair(2, ActivityType.OnFoot),
            Pair(3, ActivityType.Still),
            Pair(4, ActivityType.Unknown),
            Pair(5, ActivityType.Tilting),
            Pair(7, ActivityType.Walking),
            Pair(8, ActivityType.Running)
    )

    init {
        val intent = Intent(context, ActivityDetectorIntentService::class.java)
        this.pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        Log.v("ADBR", "Initialized using interval of ${this.detectionIntervalInMilliseconds}ms")
    }

    public override fun onReceive(context: Context?, intent: Intent?) {
        if(this.isDestroyed || intent == null || intent.action != ActivityDetectorIntentService.INTENT_ACTION_NAME) {
            Log.d("ADBR", "Skipping receive")

            return
        }

        val androidActivityType = intent.getIntExtra(ActivityDetectorIntentService.INTENT_EXTRA_TYPE_KEY, DetectedActivity.UNKNOWN)

        val activityType = this.activityTypeMapping[androidActivityType]!!

        Log.i("ADBR", "Received Intent with action ${intent.action}. Activity ${androidActivityType} => ${activityType}")

        this.activityDetectorEmitter.emit(activityType)
    }

    public fun startDetection() {
        if(this.isDestroyed) {
            throw ObjectDestroyedException()
        }

        if (this.isDetecting) {
           throw IllegalStateException("Detection already running!")
        }

        this.context.registerReceiver(this, IntentFilter(ActivityDetectorIntentService.INTENT_ACTION_NAME))

        this.activityRecognitionClient.requestActivityUpdates(this.detectionIntervalInMilliseconds.toLong(), this.pendingIntent)

        this.isDetecting = true

        Log.d("ADBR", "Detection started")
    }

    public fun stopDetection() {
        if(this.isDestroyed) {
            throw ObjectDestroyedException()
        }

        if (!this.isDetecting) {
            throw IllegalStateException("Detection is not running!")
        }

        this.activityRecognitionClient.removeActivityUpdates(this.pendingIntent)

        this.context.unregisterReceiver(this)

        this.isDetecting = false

        Log.d("ADBR", "Detection stopped")
    }

    private var isDestroyed = false
    public override fun destroy() {
        if (this.isDestroyed) {
            return
        }

        if (this.isDetecting) {
            this.stopDetection()
        }

        this.isDestroyed = true

        Log.v("ADBR", "Broadcast Receiver destroyed")
    }
}