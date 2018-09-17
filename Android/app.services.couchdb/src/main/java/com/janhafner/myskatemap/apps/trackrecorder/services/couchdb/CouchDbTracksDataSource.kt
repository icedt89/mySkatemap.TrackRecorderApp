package com.janhafner.myskatemap.apps.trackrecorder.services.couchdb

import com.couchbase.lite.Document
import com.janhafner.myskatemap.apps.trackrecorder.common.Optional
import com.janhafner.myskatemap.apps.trackrecorder.common.formatDefault
import com.janhafner.myskatemap.apps.trackrecorder.services.models.TrackInfo
import com.janhafner.myskatemap.apps.trackrecorder.services.models.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.services.track.GetTracksRequest
import com.janhafner.myskatemap.apps.trackrecorder.services.track.ITracksDataSource
import io.reactivex.Single

public final class CouchDbTracksDataSource(private val couchDbFactory: ICouchDbFactory) : ITracksDataSource {
    public override fun getTrackRecordings(request: GetTracksRequest): io.reactivex.Single<List<TrackInfo>> {
        return Single.error(NotImplementedError("SOON!"))
    }

    public override fun getTrackRecordingByIdOrNull(id: String): io.reactivex.Single<Optional<TrackRecording>> {
        return Single.fromCallable {
            var result: Document? = null

            this.couchDbFactory.executeUnitOfWork {
                result = it.getDocument(id)
            }

            if (result == null) {
                Optional<TrackRecording>(null)
            } else {
                val trackRecording = TrackRecordingConverter.trackRecordingFromCouchDbDocument(result!!)

                Optional(trackRecording)
            }
        }
    }

    public override fun saveTrackRecording(trackRecording: TrackRecording): io.reactivex.Single<String> {
        return Single.fromCallable {
            var trackRecordingId = ""
            this.couchDbFactory.executeUnitOfWork {
                val trackInfo = TrackInfo()
                trackInfo.id = trackRecording.id
                trackInfo.displayName = trackRecording.trackingStartedAt.formatDefault()
                trackInfo.trackingStartedAt = trackRecording.trackingStartedAt
                trackInfo.trackingFinishedAt = trackRecording.trackingFinishedAt!!

                val trackRecordingDocument = trackRecording.toCouchDbDocument()
                val trackInfoDocument = trackInfo.toCouchDbDocument()

                it.save(trackRecordingDocument)
                it.save(trackInfoDocument)

                trackRecordingId = trackRecordingDocument.id
            }

            trackRecordingId
        }
    }

    public override fun deleteTrackRecordingById(id: String): io.reactivex.Single<Unit> {
        return Single.fromCallable {
            this.couchDbFactory.executeUnitOfWork {
                val result = it.getDocument(id)

                it.delete(result)
            }
        }
    }
}