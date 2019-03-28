package com.janhafner.myskatemap.apps.activityrecorder.services.activityrecorder.session

import com.janhafner.myskatemap.apps.activityrecorder.core.IDestroyable
import com.janhafner.myskatemap.apps.activityrecorder.core.types.Activity
import com.janhafner.myskatemap.apps.activityrecorder.core.types.Location
import com.janhafner.myskatemap.apps.activityrecorder.infrastructure.ILocationsAggregation
import io.reactivex.Observable
import io.reactivex.Single
import org.joda.time.DateTime
import org.joda.time.Period

internal interface IActivitySession : IDestroyable {
    val distanceChanged: Observable<Float>

    val recordingTimeChanged: Observable<Period>

    val locationsChanged: Observable<Location>

    val burnedEnergyChanged: Observable<Float>

    val activityCode: String

    val stateChanged: Observable<SessionStateInfo>

    val currentState: SessionStateInfo

    val trackingStartedAt: DateTime

    val locationsAggregation: ILocationsAggregation

    fun resumeTracking()

    fun pauseTracking()

    fun discardTracking()

    fun finishTracking(): Single<Activity>
}