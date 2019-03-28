package com.janhafner.myskatemap.apps.activityrecorder.services.activity

import com.janhafner.myskatemap.apps.activityrecorder.core.types.ActivityInfo
import io.reactivex.Single

public interface IActivityQueryServiceDataSource {
    fun queryActivities(query: GetActivitiesQuery) : Single<List<ActivityInfo>>

    fun saveActivityInfo(activityInfo: ActivityInfo) : Single<String>

    fun deleteActivityInfo(activityInfoId: String): Single<Unit>
}