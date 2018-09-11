package com.janhafner.myskatemap.apps.trackrecorder.services.activitydetection

internal interface IActivityDetectorBroadcastReceiverFactory {
    fun createActivityDetector(): ActivityDetectorBroadcastReceiverBase
}