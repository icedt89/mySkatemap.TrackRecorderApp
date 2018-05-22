package com.janhafner.myskatemap.apps.trackrecorder.services

import com.janhafner.myskatemap.apps.trackrecorder.io.data.TrackRecording

internal interface ITrackService {
    fun getAllTrackRecordings(): List<TrackRecording>

    fun getTrackRecording(id: String): TrackRecording?

    fun saveTrackRecording(trackRecording: TrackRecording)

    fun deleteTrackRecording(id: String)

    fun hasTrackRecording(id: String): Boolean

    fun getAttachmentHandler(trackRecording: TrackRecording): IAttachmentHandler
}