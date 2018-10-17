package com.janhafner.myskatemap.apps.trackrecorder.services.couchdb

import com.couchbase.lite.*
import com.janhafner.myskatemap.apps.trackrecorder.common.Optional
import com.janhafner.myskatemap.apps.trackrecorder.common.formatDefault
import com.janhafner.myskatemap.apps.trackrecorder.common.types.TrackInfo
import com.janhafner.myskatemap.apps.trackrecorder.common.types.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.services.track.GetTracksRequest
import com.janhafner.myskatemap.apps.trackrecorder.services.track.ITracksDataSource
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.*

public final class CouchDbTracksDataSource(private val couchDbFactory: ICouchDbFactory) : ITracksDataSource {
    public override fun getTrackRecordings(request: GetTracksRequest): io.reactivex.Single<List<TrackInfo>> {
        return Single.fromCallable {
            val trackRecordings = ArrayList<TrackInfo>()

            this.couchDbFactory.executeUnitOfWork {
                val queryBuilder = QueryBuilder.select(SelectResult.all(), SelectResult.expression(Meta.id))
                        .from(DataSource.database(it))
                        .where(Expression.property("documentType").`is`(Expression.string(TrackInfo::class.java.simpleName)))

                val results = queryBuilder.execute()

                for (result in results) {
                    val id = UUID.fromString(result.getString("id"))
                    val dictionary = result.getDictionary(it.name)

                    val trackRecording = TrackInfoConverter.trackInfoFromCouchDbDictionary(dictionary, id)

                    trackRecordings.add(trackRecording)
                }
            }

            trackRecordings as List<TrackInfo>
        }
        .subscribeOn(Schedulers.io())
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
        .subscribeOn(Schedulers.io())
    }

    public override fun saveTrackRecording(trackRecording: TrackRecording): io.reactivex.Single<String> {
        return Single.fromCallable {
            var trackRecordingId = ""
            this.couchDbFactory.executeUnitOfWork {
                val trackInfo = TrackInfo()
                trackInfo.id = trackRecording.id
                trackInfo.displayName = trackRecording.startedAt.formatDefault()
                trackInfo.distance = 0.0f // TODO!
                trackInfo.recordingTime = trackRecording.recordingTime

                val trackRecordingDocument = trackRecording.toCouchDbDocument()
                val trackInfoDocument = trackInfo.toCouchDbDocument()

                it.save(trackRecordingDocument)
                it.save(trackInfoDocument)

                trackRecordingId = trackRecordingDocument.id
            }

            trackRecordingId
        }
        .subscribeOn(Schedulers.io())
    }

    public override fun deleteTrackRecordingById(id: String): io.reactivex.Single<Unit> {
        return Single.fromCallable {
            this.couchDbFactory.executeUnitOfWork {
                val result = it.getDocument(id)

                it.delete(result)
            }
        }
        .subscribeOn(Schedulers.io())
    }
}