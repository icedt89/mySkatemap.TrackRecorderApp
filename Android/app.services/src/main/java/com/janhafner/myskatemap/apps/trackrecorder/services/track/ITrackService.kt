package com.janhafner.myskatemap.apps.trackrecorder.services.track

import com.janhafner.myskatemap.apps.trackrecorder.common.Optional
import com.janhafner.myskatemap.apps.trackrecorder.common.types.TrackInfo
import com.janhafner.myskatemap.apps.trackrecorder.common.types.TrackRecording
import io.reactivex.Single

public interface ITrackService {
    fun getTrackRecordings() : Single<List<TrackInfo>>

    fun getTrackRecordingByIdOrNull(id: String) : Single<Optional<TrackRecording>>

    fun saveTrackRecording(trackRecording: TrackRecording) : Single<String>

    fun deleteTrackRecordingById(id: String) : Single<Unit>
}