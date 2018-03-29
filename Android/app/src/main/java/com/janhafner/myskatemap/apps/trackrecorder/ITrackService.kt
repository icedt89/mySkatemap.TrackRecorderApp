package com.janhafner.myskatemap.apps.trackrecorder

import com.janhafner.myskatemap.apps.trackrecorder.data.TrackRecording

internal interface ITrackService {
    fun hasCurrentTrackRecording(): Boolean

    fun getCurrentTrackRecording(): TrackRecording

    fun getAllTrackRecordings(includeCurrent: Boolean): List<TrackRecording>

    fun saveTrackRecording(trackRecording: TrackRecording)
}

internal final class TrackService : ITrackService {
    public override fun hasCurrentTrackRecording(): Boolean {
        TODO()
    }

    public override fun getCurrentTrackRecording(): TrackRecording {
        TODO()
    }

    public override fun getAllTrackRecordings(includeCurrent: Boolean): List<TrackRecording> {
        TODO()
    }

    public override fun saveTrackRecording(trackRecording: TrackRecording) {
        TODO()
    }
}