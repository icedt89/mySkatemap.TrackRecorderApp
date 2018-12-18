package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session

import com.janhafner.myskatemap.apps.trackrecorder.common.IDestroyable
import com.janhafner.myskatemap.apps.trackrecorder.common.types.Location
import com.janhafner.myskatemap.apps.trackrecorder.common.types.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.ILocationsAggregation
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.INewLocationsAggregation
import io.reactivex.Observable
import org.joda.time.DateTime
import org.joda.time.Period

internal interface ITrackRecordingSession : IDestroyable {
    val distanceChanged: Observable<Float>

    val recordingTimeChanged: Observable<Period>

    val locationsChanged: Observable<Location>

    val burnedEnergyChanged: Observable<Float>

    val isStillChanged: Observable<Boolean>

    val activityCode: String;

    val stateChanged: Observable<SessionStateInfo>

    val currentState: SessionStateInfo

    val sessionClosed: Observable<ITrackRecordingSession>

    val trackingStartedAt: DateTime

    val locationsAggregation: INewLocationsAggregation

    fun resumeTracking()

    fun pauseTracking()

    fun discardTracking()

    fun finishTracking(): TrackRecording
}