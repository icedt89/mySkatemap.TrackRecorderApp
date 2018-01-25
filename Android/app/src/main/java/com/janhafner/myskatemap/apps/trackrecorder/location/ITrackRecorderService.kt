package com.janhafner.myskatemap.apps.trackrecorder.location

import com.janhafner.myskatemap.apps.trackrecorder.TrackRecording

internal interface ITrackRecorderService {
    val currentSession : ITrackRecordingSession?

    fun discardTracking()

    fun finishTracking() : TrackRecording

    fun createSession(trackRecording : TrackRecording) : ITrackRecordingSession

    fun createSession(name : String) : ITrackRecordingSession
}