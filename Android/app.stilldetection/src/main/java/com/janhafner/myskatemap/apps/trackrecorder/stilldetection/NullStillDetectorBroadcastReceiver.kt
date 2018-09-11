package com.janhafner.myskatemap.apps.trackrecorder.stilldetection

import android.content.Context
import android.content.Intent

public final class NullStillDetectorBroadcastReceiver : StillDetectorBroadcastReceiverBase() {
    public override fun onReceive(context: Context?, intent: Intent?) {
    }

    public override fun startDetection() {
        if(this.isDestroyed) {
            throw IllegalStateException("Object is destroyed!")
        }

        if(this.isDetecting) {
            throw IllegalStateException("Detection already running!")
        }
    }

    public override fun stopDetection() {
        if(this.isDestroyed) {
            throw IllegalStateException("Object is destroyed!")
        }

        if(!this.isDetecting) {
            throw IllegalStateException("Detection must be started first!")
        }
    }
}