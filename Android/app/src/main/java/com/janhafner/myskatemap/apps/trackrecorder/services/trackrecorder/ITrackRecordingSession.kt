package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder

import com.janhafner.myskatemap.apps.trackrecorder.IDestroyable
import com.janhafner.myskatemap.apps.trackrecorder.io.data.Location
import com.janhafner.myskatemap.apps.trackrecorder.io.data.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.statistics.ITrackRecordingStatistic
import io.reactivex.Observable
import org.joda.time.DateTime
import org.joda.time.Period

internal interface ITrackRecordingSession :  IDestroyable {
    val trackDistanceChanged: Observable<Float>

    val recordingTimeChanged: Observable<Period>

    val locationsChanged: Observable<Location>

    val stateChanged: Observable<TrackRecorderServiceState>

    val recordingSaved: Observable<com.janhafner.myskatemap.apps.trackrecorder.Nothing>

    val sessionClosed: Observable<ITrackRecordingSession>

    val trackingStartedAt: DateTime

    var name: String

    var comment: String?

    fun resumeTracking()

    fun pauseTracking()

    fun saveTracking()

    fun discardTracking()

    fun finishTracking(): TrackRecording
}