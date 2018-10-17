package com.janhafner.myskatemap.apps.trackrecorder.activitydetection

import io.reactivex.Observable

public interface IActivityDetectorSource {
    val activityDetected : Observable<ActivityType>
}