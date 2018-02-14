package com.janhafner.myskatemap.apps.trackrecorder.location

import com.janhafner.myskatemap.apps.trackrecorder.Attachment
import com.janhafner.myskatemap.apps.trackrecorder.TrackRecording
import io.reactivex.Observable
import org.joda.time.DateTime
import org.joda.time.Period

internal interface ITrackRecordingSession {
    val trackDistanceChanged: Observable<Float>

    val recordingTimeChanged: Observable<Period>

    val locationsChanged: Observable<Location>

    val stateChanged: Observable<TrackRecorderServiceState>

    val trackingStartedAt: DateTime

    var name: String

    val attachments: MutableList<Attachment>

    fun resumeTracking()

    fun pauseTracking()

    fun saveTracking()

    fun discardTracking()

    fun finishTracking(): TrackRecording
}