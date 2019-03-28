package com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder

import android.os.Binder
import com.janhafner.myskatemap.apps.activityrecorder.core.types.Activity
import com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.session.IActivitySession
import io.reactivex.Observable

internal final class ActivityRecorderServiceBinder(private val trackRecorderService: ITrackRecorderService): Binder(), ITrackRecorderService {
    public override val hasCurrentSessionChanged: Observable<Boolean>
        get() = this.trackRecorderService.hasCurrentSessionChanged

    public override val currentSession: IActivitySession?
        get() = this.trackRecorderService.currentSession

    public override fun useActivity(activity: Activity): IActivitySession {
        return this.trackRecorderService.useActivity(activity)
    }
}