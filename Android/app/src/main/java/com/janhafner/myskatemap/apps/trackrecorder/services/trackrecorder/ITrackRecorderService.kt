package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder

import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.data.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.location.ITrackRecordingSession
import io.reactivex.Observable

internal interface ITrackRecorderService {
    val currentSession: ITrackRecordingSession?

    val locationServicesAvailability: Observable<Boolean>

    fun useTrackRecording(trackRecording: TrackRecording): ITrackRecordingSession
}