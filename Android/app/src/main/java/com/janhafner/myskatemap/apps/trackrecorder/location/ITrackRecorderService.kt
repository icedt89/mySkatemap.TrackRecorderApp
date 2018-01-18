package com.janhafner.myskatemap.apps.trackrecorder.location

import com.janhafner.myskatemap.apps.trackrecorder.TrackRecording
import io.reactivex.Observable
import org.joda.time.Duration

internal interface ITrackRecorderService {
    val locations : Observable<Location>

    val stateChanged : Observable<TrackRecorderServiceState>

    val state : TrackRecorderServiceState

    val recordingDuration: Observable<Duration>

    val trackLength: Observable<Float>

    fun resumeTracking()

    fun pauseTracking()

    fun discardTracking()

    fun saveTracking()

    fun finishTracking() : TrackRecording

    fun useTrackRecording(trackRecording : TrackRecording)

    fun useTrackRecording(name : String)
}