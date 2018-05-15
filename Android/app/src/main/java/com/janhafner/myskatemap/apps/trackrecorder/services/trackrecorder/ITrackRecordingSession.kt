package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder

import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.data.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.statistics.TrackRecordingStatistic
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.Location
import io.reactivex.Observable
import org.joda.time.DateTime
import org.joda.time.Period

internal interface ITrackRecordingSession {
    val trackDistanceChanged: Observable<Float>

    val recordingTimeChanged: Observable<Period>

    val locationsChanged: Observable<Location>

    val stateChanged: Observable<TrackRecorderServiceState>

    val trackingStartedAt: DateTime

    val statistic: TrackRecordingStatistic

    var name: String

    var comment: String?

    fun resumeTracking()

    fun pauseTracking()

    fun saveTracking()

    fun discardTracking()

    fun finishTracking(): TrackRecording
}