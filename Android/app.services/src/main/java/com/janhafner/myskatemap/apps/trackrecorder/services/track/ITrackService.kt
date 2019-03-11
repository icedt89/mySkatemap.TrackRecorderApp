package com.janhafner.myskatemap.apps.trackrecorder.services.track

import com.janhafner.myskatemap.apps.trackrecorder.core.Optional
import com.janhafner.myskatemap.apps.trackrecorder.core.types.TrackRecording
import io.reactivex.Single

public interface ITrackService {
    fun getTrackRecordingByIdOrNull(id: String) : Single<Optional<TrackRecording>>

    fun saveTrackRecording(trackRecording: TrackRecording) : Single<String>

    fun deleteTrackRecordingById(id: String) : Single<Unit>
}