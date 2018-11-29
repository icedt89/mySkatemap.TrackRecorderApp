package com.janhafner.myskatemap.apps.trackrecorder.services.track

import com.janhafner.myskatemap.apps.trackrecorder.common.types.TrackInfo
import io.reactivex.Single

public interface ITrackQueryServiceDataSource {
    fun queryTrackRecordings(query: GetTracksQuery) : Single<List<TrackInfo>>

    fun saveTrackInfo(trackInfo: TrackInfo) : Single<String>
}