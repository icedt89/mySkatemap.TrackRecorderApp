package com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder

import com.janhafner.myskatemap.apps.activityrecorder.core.types.Activity
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.session.IActivitySession
import io.reactivex.Observable

internal interface ITrackRecorderService {
    val currentSession: IActivitySession?

    val hasCurrentSessionChanged: Observable<Boolean>

    fun useActivity(activity: Activity): IActivitySession
}