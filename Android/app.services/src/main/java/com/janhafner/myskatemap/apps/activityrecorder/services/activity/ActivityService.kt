package com.janhafner.myskatemap.apps.activityrecorder.services.activity

import com.janhafner.myskatemap.apps.activityrecorder.core.Optional
import com.janhafner.myskatemap.apps.activityrecorder.core.eventing.INotifier
import com.janhafner.myskatemap.apps.activityrecorder.core.eventing.ActivityDeletedEvent
import com.janhafner.myskatemap.apps.activityrecorder.core.eventing.ActivitySavedEvent
import com.janhafner.myskatemap.apps.activityrecorder.core.types.Activity
import io.reactivex.Single

public final class ActivityService(private val localActivityServiceDataSource: IActivityServiceDataSource, private val notifier: INotifier) : IActivityService {
    public override fun getActivityByIdOrNull(id: String): Single<Optional<Activity>> {
        return this.localActivityServiceDataSource.getActivityByIdOrNull(id)
    }

    public override fun saveActivity(activity: Activity): Single<String> {
        return this.localActivityServiceDataSource.saveActivity(activity)
                .doAfterSuccess {
                    this.notifier.publish(ActivitySavedEvent(activity))
                }
    }

    public override fun deleteActivityById(id: String): Single<Unit> {
        return this.localActivityServiceDataSource.deleteActivityById(id)
                .doAfterSuccess{
                    this.notifier.publish(ActivityDeletedEvent(id))
                }
    }
}

