package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder

import com.janhafner.myskatemap.apps.trackrecorder.data.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.location.ITrackRecordingSession

internal interface ITrackRecorderService {
    val currentSession: ITrackRecordingSession?

    fun resumeSession(trackRecording: TrackRecording): ITrackRecordingSession

    fun createNewSession(name: String): ITrackRecordingSession
}