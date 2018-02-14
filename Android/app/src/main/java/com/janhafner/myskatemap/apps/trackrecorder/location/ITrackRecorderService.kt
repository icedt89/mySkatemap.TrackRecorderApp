package com.janhafner.myskatemap.apps.trackrecorder.location

import com.janhafner.myskatemap.apps.trackrecorder.TrackRecording

internal interface ITrackRecorderService {
    val currentSession: ITrackRecordingSession?

    fun resumeSession(trackRecording: TrackRecording): ITrackRecordingSession

    fun createNewSession(name: String): ITrackRecordingSession
}