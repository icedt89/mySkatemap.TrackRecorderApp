package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session

import com.janhafner.myskatemap.apps.trackrecorder.common.IDestroyable
import com.janhafner.myskatemap.apps.trackrecorder.common.types.Location
import com.janhafner.myskatemap.apps.trackrecorder.common.types.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.ILocationsAggregation
import io.reactivex.Observable
import org.joda.time.DateTime
import org.joda.time.Period

internal interface ITrackRecordingSession : IDestroyable {
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

    fun finishTracking(): TrackRecording
}