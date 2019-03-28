package com.janhafner.myskatemap.apps.activityrecorder.services.activity

import com.janhafner.myskatemap.apps.activityrecorder.core.Optional
import com.janhafner.myskatemap.apps.activityrecorder.core.types.Activity
import io.reactivex.Single

public interface IActivityService {
    fun getActivityByIdOrNull(id: String) : Single<Optional<Activity>>

    fun saveActivity(activity: Activity) : Single<String>

    fun deleteActivityById(id: String) : Single<Unit>
}