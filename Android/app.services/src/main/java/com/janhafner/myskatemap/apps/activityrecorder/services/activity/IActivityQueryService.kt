package com.janhafner.myskatemap.apps.activityrecorder.services.activity

import com.janhafner.myskatemap.apps.activityrecorder.core.types.ActivityInfo
import io.reactivex.Single

public interface IActivityQueryService {
    fun getActivities() : Single<List<ActivityInfo>>

    fun saveActivityInfo(activityInfo: ActivityInfo) : Single<String>

    fun deleteActivityInfo(activityInfoId: String): Single<Unit>
}

