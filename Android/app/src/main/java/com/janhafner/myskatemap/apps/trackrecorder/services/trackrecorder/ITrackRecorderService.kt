package com.janhafner.myskatemap.apps.trackrecorder.services.trackrecorder

import com.janhafner.myskatemap.apps.trackrecorder.data.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.location.ITrackRecordingSession
import io.reactivex.Observable

internal interface ITrackRecorderService {
    val currentSession: ITrackRecordingSession?

    val locationServicesAvailability: Observable<Boolean>

    fun resumeSession(trackRecording: TrackRecording): ITrackRecordingSession

    fun createNewSession(name: String): ITrackRecordingSession
}