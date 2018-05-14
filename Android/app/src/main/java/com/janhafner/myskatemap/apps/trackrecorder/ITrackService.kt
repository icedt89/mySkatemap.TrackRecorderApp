package com.janhafner.myskatemap.apps.trackrecorder

import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io.data.TrackRecording

internal interface ITrackService {
    fun getAllTrackRecordings(): List<TrackRecording>

    fun getTrackRecording(id: String): TrackRecording?

    fun saveTrackRecording(trackRecording: TrackRecording)

    fun deleteTrackRecording(id: String)

    fun hasTrackRecording(id: String): Boolean
}