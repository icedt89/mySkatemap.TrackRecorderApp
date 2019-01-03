package com.janhafner.myskatemap.apps.trackrecorder.services.couchdb

import com.couchbase.lite.Document
import com.janhafner.myskatemap.apps.trackrecorder.common.Optional
import com.janhafner.myskatemap.apps.trackrecorder.common.types.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.services.track.ITrackServiceDataSource
import io.reactivex.Single

public final class CouchDbTrackServiceDataSource(private val couchDbFactory: ICouchDbFactory) : ITrackServiceDataSource {
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
                val trackRecordingDocument = trackRecording.toCouchDbDocument()

                it.save(trackRecordingDocument)

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