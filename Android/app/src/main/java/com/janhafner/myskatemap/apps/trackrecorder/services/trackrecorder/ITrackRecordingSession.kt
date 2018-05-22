package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder

import com.janhafner.myskatemap.apps.trackrecorder.io.data.Location
import com.janhafner.myskatemap.apps.trackrecorder.io.data.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.statistics.TrackRecordingStatistic
import io.reactivex.Observable
import org.joda.time.DateTime
import org.joda.time.Period

internal interface ITrackRecordingSession {
    val trackDistanceChanged: Observable<Float>

    val recordingTimeChanged: Observable<Period>

    val locationsChanged: Observable<Location>

    val stateChanged: Observable<TrackRecorderServiceState>

    val recordingSaved: Observable<ITrackRecorderService>

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