package com.janhafner.myskatemap.apps.trackrecorder.services.track

import com.janhafner.myskatemap.apps.trackrecorder.core.Optional
import com.janhafner.myskatemap.apps.trackrecorder.core.eventing.INotifier
import com.janhafner.myskatemap.apps.trackrecorder.core.eventing.TrackRecordingDeletedEvent
import com.janhafner.myskatemap.apps.trackrecorder.core.eventing.TrackRecordingSavedEvent
import com.janhafner.myskatemap.apps.trackrecorder.core.types.TrackRecording
import io.reactivex.Single

public final class TrackService(private val localTrackServiceDataSource: ITrackServiceDataSource, private val notifier: INotifier) : ITrackService {
    public override fun getTrackRecordingByIdOrNull(id: String): Single<Optional<TrackRecording>> {
        return this.localTrackServiceDataSource.getTrackRecordingByIdOrNull(id)
    }

    public override fun saveTrackRecording(trackRecording: TrackRecording): Single<String> {
        return this.localTrackServiceDataSource.saveTrackRecording(trackRecording)
                .doAfterSuccess {
                    this.notifier.publish(TrackRecordingSavedEvent(trackRecording))
                }
    }

    public override fun deleteTrackRecordingById(id: String): Single<Unit> {
        return this.localTrackServiceDataSource.deleteTrackRecordingById(id)
                .doAfterSuccess{
                    this.notifier.publish(TrackRecordingDeletedEvent(id))
                }
    }
}

