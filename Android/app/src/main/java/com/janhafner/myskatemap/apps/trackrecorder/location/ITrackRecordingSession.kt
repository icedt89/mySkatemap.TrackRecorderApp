package com.janhafner.myskatemap.apps.trackrecorder.location

import io.reactivex.Observable
import org.joda.time.Period

internal interface ITrackRecordingSession {
    val trackDistanceChanged: Observable<Float>

    val recordingTimeChanged: Observable<Period>

    val locationsChanged: Observable<Location>

    val stateChanged: Observable<TrackRecorderServiceState>

    val trackDistance: Float

    val recordingTime: Period

    val state: TrackRecorderServiceState

    fun resumeTracking()

    fun pauseTracking()

    fun saveTracking()

    fun terminate()
}