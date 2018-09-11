package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder

import com.janhafner.myskatemap.apps.trackrecorder.services.models.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder.session.ITrackRecordingSession
import io.reactivex.Observable

internal interface ITrackRecorderService {
    val currentSession: ITrackRecordingSession?

    val hasCurrentSessionChanged: Observable<Boolean>

    fun useTrackRecording(trackRecording: TrackRecording): ITrackRecordingSession
}