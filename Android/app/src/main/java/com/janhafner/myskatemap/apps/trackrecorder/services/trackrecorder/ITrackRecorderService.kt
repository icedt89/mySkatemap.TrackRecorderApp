package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder

import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.data.TrackRecording

internal interface ITrackRecorderService {
    val currentSession: ITrackRecordingSession?

    fun useTrackRecording(trackRecording: TrackRecording): ITrackRecordingSession
}