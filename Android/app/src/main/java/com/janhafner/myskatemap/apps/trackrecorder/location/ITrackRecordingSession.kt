package com.janhafner.myskatemap.apps.trackrecorder.location

import io.reactivex.Observable
import org.joda.time.DateTime
import org.joda.time.Period

internal interface ITrackRecordingSession {
    val trackDistanceChanged: Observable<Float>

    val recordingTimeChanged: Observable<Period>

    val locationsChanged: Observable<Location>

    val stateChanged: Observable<TrackRecorderServiceState>

    @Deprecated("Unused")
    val trackDistance: Float

    @Deprecated("Unused")
    val recordingTime: Period

    @Deprecated("Unused")
    val state: TrackRecorderServiceState

    val trackingStartedAt: DateTime

    fun resumeTracking()

    fun pauseTracking()

    fun saveTracking()

    fun terminate()
}