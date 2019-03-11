package com.janhafner.myskatemap.apps.trackrecorder.services.track

import com.janhafner.myskatemap.apps.trackrecorder.core.eventing.INotifier
import com.janhafner.myskatemap.apps.trackrecorder.core.eventing.TrackInfoDeletedEvent
import com.janhafner.myskatemap.apps.trackrecorder.core.eventing.TrackInfoSavedEvent
import com.janhafner.myskatemap.apps.trackrecorder.core.types.TrackInfo
import io.reactivex.Single

public final class TrackQueryService(private val localTrackQueryServiceDataSource: ITrackQueryServiceDataSource, private val notifier: INotifier) : ITrackQueryService {
    public override fun getTrackRecordings(): Single<List<TrackInfo>> {
        val query = GetTracksQuery()

        return this.localTrackQueryServiceDataSource.queryTrackRecordings(query)
    }

    public override fun deleteTrackInfo(trackInfoId: String): Single<Unit> {
        return this.localTrackQueryServiceDataSource.deleteTrackInfo(trackInfoId)
                .doAfterSuccess {
                    this.notifier.publish(TrackInfoDeletedEvent(trackInfoId))
                }
    }

    public override fun saveTrackInfo(trackInfo: TrackInfo): Single<String> {
        return this.localTrackQueryServiceDataSource.saveTrackInfo(trackInfo)
                .doAfterSuccess {
                    this.notifier.publish(TrackInfoSavedEvent(trackInfo))
                }
    }
}