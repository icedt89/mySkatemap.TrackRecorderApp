package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder

import com.janhafner.myskatemap.apps.trackrecorder.io.data.TrackRecording
import io.reactivex.Observable

internal interface ITrackRecorderService {
    val currentSession: ITrackRecordingSession?

    val hasCurrentSessionChanged: Observable<Boolean>

    fun useTrackRecording(trackRecording: TrackRecording): ITrackRecordingSession
}