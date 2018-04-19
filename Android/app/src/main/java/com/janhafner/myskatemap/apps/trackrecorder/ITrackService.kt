package com.janhafner.myskatemap.apps.trackrecorder

import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.data.TrackRecording

internal interface ITrackService {
    fun hasCurrentTrackRecording(): Boolean

    fun getCurrentTrackRecording(): TrackRecording

    fun saveAsCurrentTrackRecording(trackRecording: TrackRecording)

    fun saveCurrentTrackRecording()

    fun deleteCurrentTrackRecording()

    fun getAllTrackRecordings(includeCurrent: Boolean): List<TrackRecording>
}