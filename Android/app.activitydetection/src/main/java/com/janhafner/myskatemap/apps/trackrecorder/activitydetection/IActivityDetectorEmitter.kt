package com.janhafner.myskatemap.apps.trackrecorder.activitydetection

public interface IActivityDetectorEmitter {
    fun emit(activityType: ActivityType)
}