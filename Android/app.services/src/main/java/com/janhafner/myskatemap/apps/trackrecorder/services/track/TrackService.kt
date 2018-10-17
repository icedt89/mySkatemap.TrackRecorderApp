package com.janhafner.myskatemap.apps.trackrecorder.services.track

import com.janhafner.myskatemap.apps.trackrecorder.common.Optional
import com.janhafner.myskatemap.apps.trackrecorder.common.types.TrackInfo
import com.janhafner.myskatemap.apps.trackrecorder.common.types.TrackRecording
import io.reactivex.Single

public final class TrackService(private val localTracksDataSource: ITracksDataSource) : ITrackService {
    public override fun getTrackRecordings(): Single<List<TrackInfo>> {
        val request = GetTracksRequest()

        return this.localTracksDataSource.getTrackRecordings(request)
    }

    public override fun getTrackRecordingByIdOrNull(id: String): Single<Optional<TrackRecording>> {
        return this.localTracksDataSource.getTrackRecordingByIdOrNull(id)
    }

    public override fun saveTrackRecording(trackRecording: TrackRecording): Single<String> {
        return this.localTracksDataSource.saveTrackRecording(trackRecording)
    }

    public override fun deleteTrackRecordingById(id: String): Single<Unit> {
        return this.localTracksDataSource.deleteTrackRecordingById(id)
    }
}