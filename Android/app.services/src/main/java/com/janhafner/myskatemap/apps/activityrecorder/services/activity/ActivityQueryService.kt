package com.janhafner.myskatemap.apps.activityrecorder.services.activity

import com.janhafner.myskatemap.apps.activityrecorder.core.eventing.ActivityInfoDeletedEvent
import com.janhafner.myskatemap.apps.activityrecorder.core.eventing.ActivityInfoSavedEvent
import com.janhafner.myskatemap.apps.activityrecorder.core.eventing.INotifier
import com.janhafner.myskatemap.apps.activityrecorder.core.types.ActivityInfo
import io.reactivex.Single

public final class ActivityQueryService(private val localActivityQueryServiceDataSource: IActivityQueryServiceDataSource, private val notifier: INotifier) : IActivityQueryService {
    public override fun getActivities(): Single<List<ActivityInfo>> {
        val query = GetActivitiesQuery()

        return this.localActivityQueryServiceDataSource.queryActivities(query)
    }

    public override fun deleteActivityInfo(activityInfoId: String): Single<Unit> {
        return this.localActivityQueryServiceDataSource.deleteActivityInfo(activityInfoId)
                .doAfterSuccess {
                    this.notifier.publish(ActivityInfoDeletedEvent(activityInfoId))
                }
    }

    public override fun saveActivityInfo(activityInfo: ActivityInfo): Single<String> {
        return this.localActivityQueryServiceDataSource.saveActivityInfo(activityInfo)
                .doAfterSuccess {
                    this.notifier.publish(ActivityInfoSavedEvent(activityInfo))
                }
    }
}